package com.hesung.openapi.developer.domain.service.impl;

import com.hesung.openapi.developer.controller.request.PageQuery;
import com.hesung.openapi.developer.controller.request.DeviceCommandBatchRequest;
import com.hesung.openapi.developer.controller.request.DeviceCommandRequest;
import com.hesung.openapi.developer.controller.response.DeviceCommandAcceptedResponse;
import com.hesung.openapi.developer.controller.response.DeviceCommandResultResponse;
import com.hesung.openapi.developer.controller.response.DeviceListResponse;
import com.hesung.openapi.developer.controller.response.DeviceResponse;
import com.hesung.openapi.developer.controller.response.PageResponse;
import com.hesung.openapi.developer.controller.response.ProductResponse;
import com.hesung.openapi.developer.domain.model.OpenPlatformResolvedAccessContext;
import com.hesung.openapi.developer.domain.service.OpenPlatformCapabilityService;
import com.hesung.openapi.developer.domain.service.OpenPlatformDeviceService;
import com.hesung.openapi.developer.domain.support.OpenPlatformDeviceViewAssembler;
import com.hesung.openapi.developer.domain.support.OpenPlatformPageService;
import com.hesung.openapi.developer.exception.OpenPlatformDeveloperBizExceptions;
import com.hesung.openapi.developer.exception.OpenPlatformDeveloperErrorCode;
import com.hesung.openapi.developer.infrastructure.remote.device.DeviceOpenapiClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OpenPlatformDeviceServiceImpl implements OpenPlatformDeviceService {

    private final DeviceOpenapiClient deviceOpenapiClient;
    private final OpenPlatformCapabilityService capabilityService;
    private final OpenPlatformDeviceViewAssembler deviceViewAssembler;
    private final OpenPlatformPageService pageService;
    private final Map<String, DeviceCommandResultResponse> commandResults = new ConcurrentHashMap<>();

    public OpenPlatformDeviceServiceImpl(DeviceOpenapiClient deviceOpenapiClient,
                                         OpenPlatformCapabilityService capabilityService,
                                         OpenPlatformDeviceViewAssembler deviceViewAssembler,
                                         OpenPlatformPageService pageService) {
        this.deviceOpenapiClient = deviceOpenapiClient;
        this.capabilityService = capabilityService;
        this.deviceViewAssembler = deviceViewAssembler;
        this.pageService = pageService;
    }

    @Override
    public PageResponse<DeviceListResponse> queryDevicePage(OpenPlatformResolvedAccessContext accessContext,
                                                            PageQuery pageQuery) {
        return pageService.paginate(queryAccessibleDevices(accessContext), pageQuery);
    }

    @Override
    public PageResponse<ProductResponse> queryProductPage(PageQuery pageQuery) {
        List<ProductResponse> products = new ArrayList<>(queryPlatformDevices().stream()
                .collect(Collectors.toMap(
                        deviceViewAssembler::productKey,
                        this::buildProductView,
                        (left, right) -> left,
                        LinkedHashMap::new
                ))
                .values());
        return pageService.paginate(products, pageQuery);
    }

    @Override
    public ProductResponse queryProductDetail(String productId) {
        return queryPlatformDevices().stream()
                .filter(device -> productId.equals(deviceViewAssembler.productKey(device)))
                .findFirst()
                .map(this::buildProductView)
                .orElse(null);
    }

    @Override
    public DeviceResponse queryDeviceDetail(OpenPlatformResolvedAccessContext accessContext, String deviceId) {
        DeviceListResponse summary = requireAccessibleDevice(accessContext, deviceId);
        Map<String, Object> rawState = loadRawDeviceState(accessContext, deviceId);
        return deviceViewAssembler.toDeviceDetailResponse(summary, rawState);
    }

    @Override
    public Map<String, Object> queryDeviceState(OpenPlatformResolvedAccessContext accessContext, String deviceId) {
        return capabilityService.normalizeState(loadRawDeviceState(accessContext, deviceId));
    }

    @Override
    public List<DeviceResponse> queryBatchDevices(OpenPlatformResolvedAccessContext accessContext, List<String> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return Collections.emptyList();
        }
        return accessContext.filterAllowedDeviceIds(deviceIds).stream()
                .filter(StringUtils::hasText)
                .map(deviceId -> queryDeviceDetail(accessContext, deviceId))
                .collect(Collectors.toList());
    }

    @Override
    public DeviceCommandAcceptedResponse controlDevice(OpenPlatformResolvedAccessContext accessContext,
                                                       String deviceId,
                                                       DeviceCommandBatchRequest request) {
        Map<String, Object> standardDesired = new LinkedHashMap<>();
        if (request != null && request.getCommands() != null) {
            for (DeviceCommandRequest command : request.getCommands()) {
                if (command == null || command.getCapability() == null) {
                    continue;
                }
                String capability = command.getCapability().trim();
                if (!capabilityService.findCapability(capability).isPresent()) {
                    throw OpenPlatformDeveloperBizExceptions.of(
                            OpenPlatformDeveloperErrorCode.DEVICE_CAPABILITY_UNSUPPORTED,
                            OpenPlatformDeveloperErrorCode.DEVICE_CAPABILITY_UNSUPPORTED.getErrorMessage() + ": " + capability
                    );
                }
                standardDesired.put(capability, command.getValue());
            }
        }

        Map<String, Object> desired = capabilityService.toDesired(standardDesired);
        if (desired.isEmpty()) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.DEVICE_COMMANDS_EMPTY);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("devicesn", deviceId);
        payload.put("desired", desired);

        Boolean accepted = deviceOpenapiClient.controlDevice(applicationContextHeader(accessContext), payload);
        String commandId = "cmd_" + UUID.randomUUID().toString().replace("-", "");
        String now = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        DeviceCommandResultResponse result = DeviceCommandResultResponse.builder()
                .commandId(commandId)
                .deviceId(deviceId)
                .status(Boolean.TRUE.equals(accepted) ? "succeeded" : "failed")
                .accepted(Boolean.TRUE.equals(accepted))
                .desired(standardDesired)
                .createdAt(now)
                .updatedAt(now)
                .build();
        commandResults.put(commandId, result);

        return DeviceCommandAcceptedResponse.builder()
                .accepted(Boolean.TRUE.equals(accepted))
                .commandId(commandId)
                .build();
    }

    @Override
    public DeviceCommandResultResponse getCommandResult(OpenPlatformResolvedAccessContext accessContext,
                                                        String deviceId,
                                                        String commandId) {
        DeviceCommandResultResponse result = commandResults.get(commandId);
        if (result == null || (StringUtils.hasText(deviceId) && !deviceId.equals(result.getDeviceId()))) {
            return DeviceCommandResultResponse.builder()
                    .commandId(commandId)
                    .deviceId(deviceId)
                    .status("unknown")
                    .accepted(false)
                    .build();
        }
        return result;
    }

    @Override
    public Map<String, Map<String, Object>> queryBatchStates(OpenPlatformResolvedAccessContext accessContext,
                                                             List<String> deviceIds) {
        Map<String, Map<String, Object>> states = new LinkedHashMap<>();
        if (deviceIds == null) {
            return states;
        }
        for (String deviceId : accessContext.filterAllowedDeviceIds(deviceIds)) {
            if (StringUtils.hasText(deviceId)) {
                states.put(deviceId, queryDeviceState(accessContext, deviceId));
            }
        }
        return states;
    }

    @Override
    public Map<String, Object> createResyncTask(OpenPlatformResolvedAccessContext accessContext, List<String> deviceIds) {
        List<String> safeIds = deviceIds == null ? Collections.emptyList() : deviceIds.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        if (safeIds.isEmpty()) {
            safeIds = queryAccessibleDevices(accessContext).stream().map(DeviceListResponse::getDeviceId).collect(Collectors.toList());
        } else {
            safeIds = accessContext.filterAllowedDeviceIds(safeIds);
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("syncId", "sync_" + UUID.randomUUID().toString().replace("-", ""));
        response.put("accepted", true);
        response.put("deviceIds", safeIds);
        response.put("requestedAt", OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        return response;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> extractRequestedDeviceIds(Map<String, Object> request) {
        if (request == null) {
            return Collections.emptyList();
        }
        Object ids = request.get("deviceIds");
        if (ids instanceof List) {
            return ((List<Object>) ids).stream().map(String::valueOf).collect(Collectors.toList());
        }
        Object single = request.get("deviceId");
        if (single != null) {
            return Arrays.asList(String.valueOf(single));
        }
        return Collections.emptyList();
    }

    @Override
    public String extractRequestGrantId(Map<String, Object> request) {
        if (request == null) {
            return null;
        }
        Object grantId = request.get("grantId");
        return grantId == null ? null : String.valueOf(grantId);
    }

    private String applicationContextHeader(OpenPlatformResolvedAccessContext accessContext) {
        return accessContext == null ? null : accessContext.getApplicationContextHeader();
    }

    private List<DeviceListResponse> queryPlatformDevices() {
        return queryAccessibleDevices(null);
    }

    private List<DeviceListResponse> queryAccessibleDevices(OpenPlatformResolvedAccessContext accessContext) {
        List<Map<String, Object>> rawDevices = deviceOpenapiClient.listDevices(applicationContextHeader(accessContext));
        if (rawDevices == null || rawDevices.isEmpty()) {
            return Collections.emptyList();
        }
        List<DeviceListResponse> devices = rawDevices.stream()
                .map(deviceViewAssembler::toDeviceListResponse)
                .collect(Collectors.toList());
        if (accessContext == null || !accessContext.requiresDeviceFiltering()) {
            return devices;
        }
        return devices.stream()
                .filter(item -> accessContext.allowsDevice(item.getDeviceId()))
                .collect(Collectors.toList());
    }

    private DeviceListResponse requireAccessibleDevice(OpenPlatformResolvedAccessContext accessContext, String deviceId) {
        return queryAccessibleDevices(accessContext).stream()
                .filter(item -> deviceId.equals(item.getDeviceId()))
                .findFirst()
                .orElseThrow(() -> OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.DEVICE_NOT_FOUND));
    }

    private Map<String, Object> loadRawDeviceState(OpenPlatformResolvedAccessContext accessContext, String deviceId) {
        Object response = deviceOpenapiClient.getDeviceState(applicationContextHeader(accessContext), deviceId);
        return deviceViewAssembler.extractDeviceState(response);
    }

    private ProductResponse buildProductView(DeviceListResponse device) {
        return deviceViewAssembler.toProductResponse(device, loadRawDeviceState(null, device.getDeviceId()));
    }
}

package com.hesung.openapi.developer.application;

import com.hesung.openapi.developer.controller.request.DeviceCommandBatchRequest;
import com.hesung.openapi.developer.controller.request.PageQuery;
import com.hesung.openapi.developer.controller.response.CapabilityDefinitionResponse;
import com.hesung.openapi.developer.controller.response.DeviceCommandAcceptedResponse;
import com.hesung.openapi.developer.controller.response.DeviceCommandResultResponse;
import com.hesung.openapi.developer.controller.response.DeviceListResponse;
import com.hesung.openapi.developer.controller.response.DeviceResponse;
import com.hesung.openapi.developer.controller.response.PageResponse;
import com.hesung.openapi.developer.controller.response.ProductResponse;
import com.hesung.openapi.developer.domain.service.OpenPlatformAccessResolver;
import com.hesung.openapi.developer.domain.service.OpenPlatformCapabilityService;
import com.hesung.openapi.developer.domain.service.OpenPlatformDeviceService;
import com.hesung.openapi.developer.exception.OpenPlatformDeveloperBizExceptions;
import com.hesung.openapi.developer.exception.OpenPlatformDeveloperErrorCode;
import com.hesung.openapi.developer.model.OpenPlatformCallerContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OpenPlatformDeviceApplicationService {

    private final OpenPlatformDeviceService deviceService;
    private final OpenPlatformCapabilityService capabilityService;
    private final OpenPlatformAccessResolver accessResolver;

    public OpenPlatformDeviceApplicationService(OpenPlatformDeviceService deviceService,
                                                OpenPlatformCapabilityService capabilityService,
                                                OpenPlatformAccessResolver accessResolver) {
        this.deviceService = deviceService;
        this.capabilityService = capabilityService;
        this.accessResolver = accessResolver;
    }

    public PageResponse<DeviceListResponse> queryDevicePage(OpenPlatformCallerContext callerContext,
                                                            String grantId,
                                                            PageQuery pageQuery) {
        requireScope(callerContext, "device.read");
        return deviceService.queryDevicePage(
                accessResolver.resolveDeviceAccess(callerContext, grantId, null, "device.read"),
                pageQuery
        );
    }

    public DeviceResponse queryDeviceDetail(OpenPlatformCallerContext callerContext, String deviceId, String grantId) {
        requireScope(callerContext, "device.read");
        return deviceService.queryDeviceDetail(
                accessResolver.resolveDeviceAccess(callerContext, grantId, deviceId, "device.read"),
                deviceId
        );
    }

    public Map<String, Object> queryDeviceState(OpenPlatformCallerContext callerContext, String deviceId, String grantId) {
        requireScope(callerContext, "device.read");
        return deviceService.queryDeviceState(
                accessResolver.resolveDeviceAccess(callerContext, grantId, deviceId, "device.read"),
                deviceId
        );
    }

    public DeviceCommandAcceptedResponse controlDevice(OpenPlatformCallerContext callerContext,
                                                       String deviceId,
                                                       DeviceCommandBatchRequest commandRequest) {
        requireScope(callerContext, "device.control");
        return deviceService.controlDevice(
                accessResolver.resolveDeviceAccess(
                        callerContext,
                        commandRequest == null ? null : commandRequest.getGrantId(),
                        deviceId,
                        "device.control"
                ),
                deviceId,
                commandRequest
        );
    }

    public DeviceCommandResultResponse getCommandResult(OpenPlatformCallerContext callerContext,
                                                        String deviceId,
                                                        String commandId,
                                                        String grantId) {
        requireScope(callerContext, "device.read");
        return deviceService.getCommandResult(
                accessResolver.resolveDeviceAccess(callerContext, grantId, deviceId, "device.read"),
                deviceId,
                commandId
        );
    }

    public List<DeviceResponse> queryBatchDevices(OpenPlatformCallerContext callerContext, Map<String, Object> body) {
        requireScope(callerContext, "device.read");
        return deviceService.queryBatchDevices(
                accessResolver.resolveDeviceAccess(
                        callerContext,
                        deviceService.extractRequestGrantId(body),
                        null,
                        "device.read"
                ),
                deviceService.extractRequestedDeviceIds(body)
        );
    }

    public Map<String, Map<String, Object>> queryBatchStates(OpenPlatformCallerContext callerContext, Map<String, Object> body) {
        requireScope(callerContext, "device.read");
        return deviceService.queryBatchStates(
                accessResolver.resolveDeviceAccess(
                        callerContext,
                        deviceService.extractRequestGrantId(body),
                        null,
                        "device.read"
                ),
                deviceService.extractRequestedDeviceIds(body)
        );
    }

    public Map<String, Object> createResyncTask(OpenPlatformCallerContext callerContext, Map<String, Object> body) {
        requireScope(callerContext, "device.read");
        return deviceService.createResyncTask(
                accessResolver.resolveDeviceAccess(
                        callerContext,
                        deviceService.extractRequestGrantId(body),
                        null,
                        "device.read"
                ),
                deviceService.extractRequestedDeviceIds(body)
        );
    }

    public List<CapabilityDefinitionResponse> listCapabilities(OpenPlatformCallerContext callerContext) {
        requireScope(callerContext, "capability.read", "device.read");
        return capabilityService.listCapabilities();
    }

    public CapabilityDefinitionResponse getCapability(OpenPlatformCallerContext callerContext, String capabilityCode) {
        requireScope(callerContext, "capability.read", "device.read");
        return capabilityService.getCapability(capabilityCode);
    }

    public PageResponse<ProductResponse> queryProductPage(OpenPlatformCallerContext callerContext, PageQuery pageQuery) {
        requireScope(callerContext, "product.read", "device.read");
        return deviceService.queryProductPage(pageQuery);
    }

    public ProductResponse queryProductDetail(OpenPlatformCallerContext callerContext, String productId) {
        requireScope(callerContext, "product.read", "device.read");
        return deviceService.queryProductDetail(productId);
    }

    private void requireScope(OpenPlatformCallerContext callerContext, String... scopes) {
        if (scopes == null || scopes.length == 0) {
            return;
        }
        for (String scope : scopes) {
            if (callerContext.hasScope(scope)) {
                return;
            }
        }
        throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.INSUFFICIENT_SCOPE);
    }
}

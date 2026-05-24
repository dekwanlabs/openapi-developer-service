package com.hesung.openapi.developer.domain.support;

import com.hesung.openapi.developer.controller.response.DeviceCapabilityResponse;
import com.hesung.openapi.developer.controller.response.DeviceFirmwareResponse;
import com.hesung.openapi.developer.controller.response.DeviceListResponse;
import com.hesung.openapi.developer.controller.response.DeviceResponse;
import com.hesung.openapi.developer.controller.response.ProductResponse;
import com.hesung.openapi.developer.domain.service.OpenPlatformCapabilityService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenPlatformDeviceViewAssembler {

    private final OpenPlatformCapabilityService capabilityService;

    public OpenPlatformDeviceViewAssembler(OpenPlatformCapabilityService capabilityService) {
        this.capabilityService = capabilityService;
    }

    public DeviceListResponse toDeviceListResponse(Map<String, Object> source) {
        Map<String, Object> rawState = safeMap(source.get("state"));
        Map<String, Object> normalizedState = capabilityService.normalizeState(rawState);
        return DeviceListResponse.builder()
                .deviceId(firstString(source, "deviceSn", "devicesn", "deviceId", "deviceid", "sn", "id"))
                .productId(firstString(source, "productId", "productid", "productModel", "model"))
                .model(stringValue(source.get("model")))
                .deviceType(stringValue(source.get("deviceType")))
                .deviceName(firstString(source, "deviceName", "name"))
                .online(!rawState.isEmpty())
                .capabilities(capabilityService.inferCapabilityCodes(normalizedState))
                .build();
    }

    public DeviceResponse toDeviceDetailResponse(DeviceListResponse summary, Map<String, Object> rawState) {
        Map<String, Object> normalizedState = capabilityService.normalizeState(rawState);
        List<DeviceCapabilityResponse> capabilities = capabilityService.toDeviceCapabilities(normalizedState);
        return DeviceResponse.builder()
                .deviceId(summary == null ? null : summary.getDeviceId())
                .productId(summary == null ? null : summary.getProductId())
                .model(summary == null ? null : summary.getModel())
                .deviceType(summary == null ? null : summary.getDeviceType())
                .deviceName(summary == null ? null : summary.getDeviceName())
                .firmware(DeviceFirmwareResponse.builder()
                        .mcuHardwareModel(stringValue(rawState.get("mcu_hardware_model")))
                        .mcuFirmwareVersion(stringValue(rawState.get("mcu_firmware_version")))
                        .moduleFirmwareVersion(stringValue(rawState.get("module_firmware_version")))
                        .build())
                .capabilities(capabilities)
                .state(normalizedState)
                .build();
    }

    public ProductResponse toProductResponse(DeviceListResponse device, Map<String, Object> rawState) {
        return ProductResponse.builder()
                .productId(productKey(device))
                .model(device == null ? null : device.getModel())
                .deviceType(device == null ? null : device.getDeviceType())
                .name(device == null ? null : device.getModel())
                .capabilities(capabilityService.toDeviceCapabilities(capabilityService.normalizeState(rawState)))
                .build();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> extractDeviceState(Object response) {
        if (!(response instanceof Map)) {
            return Collections.emptyMap();
        }
        Map<String, Object> responseMap = (Map<String, Object>) response;
        Object data = responseMap.get("data");
        if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            Object nestedState = dataMap.get("state");
            if (nestedState instanceof Map) {
                return (Map<String, Object>) nestedState;
            }
            return dataMap;
        }
        Object nestedState = responseMap.get("state");
        if (nestedState instanceof Map) {
            return (Map<String, Object>) nestedState;
        }
        return responseMap;
    }

    public String productKey(DeviceListResponse device) {
        if (device == null) {
            return null;
        }
        if (StringUtils.hasText(device.getProductId())) {
            return device.getProductId();
        }
        if (StringUtils.hasText(device.getModel())) {
            return device.getModel();
        }
        if (StringUtils.hasText(device.getDeviceType())) {
            return device.getDeviceType();
        }
        return "unknown";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> safeMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return Collections.emptyMap();
    }

    private String firstString(Map<String, Object> source, String... keys) {
        if (source == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            String value = stringValue(source.get(key));
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}

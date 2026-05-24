package com.hesung.openapi.developer.controller.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DeviceResponse {

    private String deviceId;

    private String productId;

    private String model;

    private String deviceType;

    private String deviceName;

    private DeviceFirmwareResponse firmware;

    private List<DeviceCapabilityResponse> capabilities;

    private Map<String, Object> state;
}

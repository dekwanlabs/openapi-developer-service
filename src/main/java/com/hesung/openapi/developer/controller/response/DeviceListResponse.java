package com.hesung.openapi.developer.controller.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DeviceListResponse {

    private String deviceId;

    private String productId;

    private String model;

    private String deviceType;

    private String deviceName;

    private Boolean online;

    private List<String> capabilities;
}

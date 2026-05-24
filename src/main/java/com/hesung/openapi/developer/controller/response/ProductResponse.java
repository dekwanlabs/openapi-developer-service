package com.hesung.openapi.developer.controller.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductResponse {

    private String productId;

    private String model;

    private String deviceType;

    private String name;

    private List<DeviceCapabilityResponse> capabilities;
}

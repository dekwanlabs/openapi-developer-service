package com.hesung.openapi.developer.controller.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DeviceCommandResultResponse {

    private String commandId;

    private String deviceId;

    private String status;

    private Boolean accepted;

    private Map<String, Object> desired;

    private String createdAt;

    private String updatedAt;
}

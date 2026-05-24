package com.hesung.openapi.developer.controller.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceCommandAcceptedResponse {

    private Boolean accepted;

    private String commandId;
}

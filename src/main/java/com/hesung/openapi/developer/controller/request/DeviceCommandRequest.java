package com.hesung.openapi.developer.controller.request;

import lombok.Data;

@Data
public class DeviceCommandRequest {

    private String capability;

    private Object value;
}

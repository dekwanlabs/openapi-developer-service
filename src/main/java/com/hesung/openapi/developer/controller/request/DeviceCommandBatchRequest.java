package com.hesung.openapi.developer.controller.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DeviceCommandBatchRequest {

    private String grantId;

    private List<DeviceCommandRequest> commands = new ArrayList<>();
}

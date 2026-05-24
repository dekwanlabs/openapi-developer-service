package com.hesung.openapi.developer.controller.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceFirmwareResponse {

    private String mcuHardwareModel;

    private String mcuFirmwareVersion;

    private String moduleFirmwareVersion;
}

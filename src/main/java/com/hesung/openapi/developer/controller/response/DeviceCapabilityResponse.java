package com.hesung.openapi.developer.controller.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DeviceCapabilityResponse {

    private String code;

    private String type;

    private Boolean readable;

    private Boolean writable;

    private Integer min;

    private Integer max;

    private Integer step;

    private List<String> values;
}

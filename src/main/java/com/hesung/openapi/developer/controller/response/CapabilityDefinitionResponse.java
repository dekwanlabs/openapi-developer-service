package com.hesung.openapi.developer.controller.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CapabilityDefinitionResponse {

    private String code;

    private String name;

    private String type;

    private String description;

    private Boolean readable;

    private Boolean writable;

    private String unit;

    private Integer min;

    private Integer max;

    private Integer step;

    private List<String> values;
}

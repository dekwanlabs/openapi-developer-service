package com.hesung.openapi.developer.dao.entity;

import lombok.Data;

@Data
public class OpenapiAppEntity {

    private Long id;

    private String appId;

    private String clientId;

    private String appName;

    private String appType;

    private String status;
}

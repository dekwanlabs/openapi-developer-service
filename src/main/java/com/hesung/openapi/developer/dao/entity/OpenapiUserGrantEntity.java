package com.hesung.openapi.developer.dao.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OpenapiUserGrantEntity {

    private Long id;

    private String grantId;

    private String appId;

    private String userId;

    private String region;

    private String grantScopeType;

    private String deviceIds;

    private String scopes;

    private String status;

    private LocalDateTime revokedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

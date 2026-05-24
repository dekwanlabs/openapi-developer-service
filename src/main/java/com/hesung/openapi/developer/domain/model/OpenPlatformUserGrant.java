package com.hesung.openapi.developer.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class OpenPlatformUserGrant {

    private final String grantId;

    private final String appId;

    private final String userId;

    private final String region;

    private final String grantScopeType;

    private final String deviceIds;

    private final String scopes;

    private final String status;

    private final LocalDateTime revokedAt;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;
}

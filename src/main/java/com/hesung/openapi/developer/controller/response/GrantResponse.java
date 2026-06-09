package com.hesung.openapi.developer.controller.response;

import com.hesung.openapi.developer.dao.entity.OpenapiUserGrantEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class GrantResponse {

    private final String grantId;
    private final String appId;
    private final String userId;
    private final String region;
    private final String grantScopeType;
    private final List<String> deviceIds;
    private final List<String> scopes;
    private final String status;

    public static GrantResponse from(OpenapiUserGrantEntity entity) {
        return GrantResponse.builder()
                .grantId(entity.getGrantId())
                .appId(entity.getAppId())
                .userId(entity.getUserId())
                .region(entity.getRegion())
                .grantScopeType(entity.getGrantScopeType())
                .deviceIds(entity.getDeviceIds() == null
                        ? Collections.emptyList()
                        : Arrays.asList(entity.getDeviceIds().split(",")))
                .scopes(entity.getScopes() == null
                        ? Collections.emptyList()
                        : Arrays.asList(entity.getScopes().split(",")))
                .status(entity.getStatus())
                .build();
    }
}

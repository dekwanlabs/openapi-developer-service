package com.hesung.openapi.developer.controller.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GrantResponse {

    private String grantId;

    private String appId;

    private String userId;

    private String region;

    private String grantScopeType;

    private List<String> deviceIds;

    private String status;

    private List<String> scopes;

    private String grantedAt;

    private String updatedAt;

    private String revokedAt;
}

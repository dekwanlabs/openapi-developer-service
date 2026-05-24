package com.hesung.openapi.developer.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OpenPlatformAppProfile {

    private final String appId;

    private final String clientId;

    private final String appName;

    private final String appType;

    private final String status;
}

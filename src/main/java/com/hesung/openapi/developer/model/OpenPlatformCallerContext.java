package com.hesung.openapi.developer.model;

import com.hesung.openapi.developer.exception.OpenPlatformDeveloperBizExceptions;
import com.hesung.openapi.developer.exception.OpenPlatformDeveloperErrorCode;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class OpenPlatformCallerContext {

    private final String clientId;

    private final String appId;

    private final String userId;

    private final String region;

    private final List<String> scopes;

    private final String applicationContextHeader;

    public boolean isUserToken() {
        return StringUtils.hasText(userId);
    }

    public boolean isAppToken() {
        return StringUtils.hasText(clientId) && !isUserToken();
    }

    public String requireAppId() {
        if (!StringUtils.hasText(appId)) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.APP_IDENTITY_NOT_FOUND);
        }
        return appId;
    }

    public String currentRegion() {
        return StringUtils.hasText(region) ? region : "US";
    }

    public List<String> currentScopes() {
        return scopes == null ? Collections.emptyList() : scopes;
    }

    public boolean hasScope(String scope) {
        return currentScopes().contains(scope);
    }

    public String stableUserId() {
        return StringUtils.hasText(userId) ? userId : "current-user";
    }

    public String stableAppId() {
        return StringUtils.hasText(appId) ? appId : "current-app";
    }
}

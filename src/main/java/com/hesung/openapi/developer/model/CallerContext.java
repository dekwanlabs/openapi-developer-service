package com.hesung.openapi.developer.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Request-scoped caller identity, resolved from the gateway-set
 * Application-Context header by {@link CallerContextResolver}.
 */
@Getter
@Builder
public class CallerContext {

    /** OAuth client_id from the access token. */
    private final String clientId;

    /** Application ID associated with the client. */
    private final String appId;

    /** Dreo user ID. Non-null = user token; null = app token. */
    private final String userId;

    /** Region from token, defaults to "US". */
    private final String region;

    /** Authorized scopes. */
    private final List<String> scopes;

    public boolean isUserToken() {
        return StringUtils.hasText(userId);
    }

    public String requireAppId() {
        if (!StringUtils.hasText(appId)) {
            throw new IllegalArgumentException("appId is required");
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
}

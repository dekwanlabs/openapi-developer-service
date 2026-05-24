package com.hesung.openapi.developer.domain.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
public class OpenPlatformResolvedAccessContext {

    private final String tokenType;

    private final String appId;

    private final String clientId;

    private final String userId;

    private final String region;

    private final String grantId;

    private final String grantScopeType;

    private final List<String> scopes;

    private final List<String> deviceIds;

    private final String applicationContextHeader;

    public boolean isUserToken() {
        return "user".equals(tokenType);
    }

    public boolean requiresDeviceFiltering() {
        return "selected_devices".equals(grantScopeType) && deviceIds != null && !deviceIds.isEmpty();
    }

    public boolean allowsDevice(String deviceId) {
        if (!StringUtils.hasText(deviceId) || !requiresDeviceFiltering()) {
            return true;
        }
        return deviceIds.contains(deviceId);
    }

    public List<String> filterAllowedDeviceIds(List<String> requestedDeviceIds) {
        if (requestedDeviceIds == null || requestedDeviceIds.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> normalized = requestedDeviceIds.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!requiresDeviceFiltering()) {
            return normalized.stream().collect(Collectors.toList());
        }
        return normalized.stream()
                .filter(deviceIds::contains)
                .collect(Collectors.toList());
    }
}

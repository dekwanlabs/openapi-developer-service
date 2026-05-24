package com.hesung.openapi.developer.domain.service.impl;

import com.hesung.openapi.developer.domain.service.OpenPlatformAccessResolver;
import com.hesung.openapi.developer.domain.service.OpenPlatformGrantService;
import com.hesung.openapi.developer.domain.model.OpenPlatformResolvedAccessContext;
import com.hesung.openapi.developer.domain.model.OpenPlatformUserGrant;
import com.hesung.openapi.developer.exception.OpenPlatformDeveloperBizExceptions;
import com.hesung.openapi.developer.exception.OpenPlatformDeveloperErrorCode;
import com.hesung.openapi.developer.infrastructure.web.OpenPlatformRequestContext;
import com.hesung.openapi.developer.model.OpenPlatformCallerContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OpenPlatformAccessResolverImpl implements OpenPlatformAccessResolver {

    private final OpenPlatformRequestContext requestContext;
    private final OpenPlatformGrantService grantService;

    public OpenPlatformAccessResolverImpl(OpenPlatformRequestContext requestContext,
                                          OpenPlatformGrantService grantService) {
        this.requestContext = requestContext;
        this.grantService = grantService;
    }

    @Override
    public OpenPlatformResolvedAccessContext resolveDeviceAccess(OpenPlatformCallerContext callerContext,
                                                                 String grantId,
                                                                 String deviceId,
                                                                 String requiredScope) {
        if (callerContext.isUserToken()) {
            return OpenPlatformResolvedAccessContext.builder()
                    .tokenType("user")
                    .appId(callerContext.getAppId())
                    .clientId(callerContext.getClientId())
                    .userId(callerContext.getUserId())
                    .region(callerContext.currentRegion())
                    .grantId(grantId)
                    .grantScopeType("all_devices")
                    .scopes(callerContext.currentScopes())
                    .deviceIds(Collections.emptyList())
                    .applicationContextHeader(callerContext.getApplicationContextHeader())
                    .build();
        }

        String appId = callerContext.requireAppId();
        if (!StringUtils.hasText(grantId)) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.GRANT_ID_REQUIRED);
        }

        OpenPlatformUserGrant grant = grantService.requireActiveGrant(appId, grantId.trim());
        List<String> scopes = split(grant.getScopes());
        if (StringUtils.hasText(requiredScope) && !scopes.contains(requiredScope)) {
            throw OpenPlatformDeveloperBizExceptions.of(
                    OpenPlatformDeveloperErrorCode.GRANT_SCOPE_MISSING,
                    OpenPlatformDeveloperErrorCode.GRANT_SCOPE_MISSING.getErrorMessage() + ": " + requiredScope
            );
        }

        List<String> deviceIds = split(grant.getDeviceIds());
        if (StringUtils.hasText(deviceId)
                && "selected_devices".equals(grant.getGrantScopeType())
                && !deviceIds.contains(deviceId)) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.DEVICE_OUTSIDE_GRANT_SCOPE);
        }

        return OpenPlatformResolvedAccessContext.builder()
                .tokenType("app")
                .appId(appId)
                .clientId(callerContext.getClientId())
                .userId(grant.getUserId())
                .region(StringUtils.hasText(grant.getRegion()) ? grant.getRegion() : callerContext.currentRegion())
                .grantId(grant.getGrantId())
                .grantScopeType(grant.getGrantScopeType())
                .scopes(scopes)
                .deviceIds(deviceIds)
                .applicationContextHeader(
                        requestContext.buildUserApplicationContextHeader(
                                callerContext,
                                grant.getUserId(),
                                StringUtils.hasText(grant.getRegion()) ? grant.getRegion() : callerContext.currentRegion()
                        )
                )
                .build();
    }

    private List<String> split(String value) {
        if (!StringUtils.hasText(value)) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toList());
    }
}

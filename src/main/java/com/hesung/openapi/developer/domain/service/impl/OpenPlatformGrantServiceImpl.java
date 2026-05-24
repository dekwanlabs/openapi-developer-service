package com.hesung.openapi.developer.domain.service.impl;

import com.hesung.openapi.developer.controller.request.GrantCreateRequest;
import com.hesung.openapi.developer.controller.response.GrantResponse;
import com.hesung.openapi.developer.domain.model.OpenPlatformUserGrant;
import com.hesung.openapi.developer.domain.service.OpenPlatformGrantService;
import com.hesung.openapi.developer.domain.service.OpenPlatformUserAccountService;
import com.hesung.openapi.developer.exception.OpenPlatformDeveloperBizExceptions;
import com.hesung.openapi.developer.exception.OpenPlatformDeveloperErrorCode;
import com.hesung.openapi.developer.infrastructure.persistence.OpenPlatformGrantRepository;
import com.hesung.openapi.developer.infrastructure.remote.user.dto.UserAccountInfo;
import com.hesung.openapi.developer.infrastructure.web.OpenPlatformRequestContext;
import com.hesung.openapi.developer.model.OpenPlatformCallerContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OpenPlatformGrantServiceImpl implements OpenPlatformGrantService {

    private static final String STATUS_ACTIVE = "active";

    private static final String STATUS_REVOKED = "revoked";

    private static final List<String> SUPPORTED_GRANT_SCOPES = Arrays.asList(
            "device.read",
            "device.control",
            "device.event.read"
    );

    private final OpenPlatformGrantRepository grantRepository;
    private final OpenPlatformRequestContext requestContext;
    private final OpenPlatformUserAccountService userAccountService;

    public OpenPlatformGrantServiceImpl(OpenPlatformGrantRepository grantRepository,
                                        OpenPlatformRequestContext requestContext,
                                        OpenPlatformUserAccountService userAccountService) {
        this.grantRepository = grantRepository;
        this.requestContext = requestContext;
        this.userAccountService = userAccountService;
    }

    @Override
    public GrantResponse createGrant(OpenPlatformCallerContext callerContext, GrantCreateRequest createRequest) {
        String appId = callerContext.requireAppId();
        UserAccountInfo accountInfo = userAccountService.resolveRequired(
                createRequest == null ? null : createRequest.getUserId(),
                createRequest == null ? null : createRequest.getUserAccount()
        );
        String userId = String.valueOf(accountInfo.getId());
        String region = StringUtils.hasText(createRequest == null ? null : createRequest.getRegion())
                ? createRequest.getRegion().trim()
                : callerContext.currentRegion();
        String grantScopeType = normalizeGrantScopeType(createRequest == null ? null : createRequest.getGrantScopeType());
        List<String> deviceIds = normalizeDeviceIds(createRequest == null ? null : createRequest.getDeviceIds());
        List<String> scopes = normalizeScopes(createRequest == null ? null : createRequest.getScopes());
        validateGrantScope(grantScopeType, deviceIds);

        LocalDateTime now = LocalDateTime.now();
        OpenPlatformUserGrant grant = OpenPlatformUserGrant.builder()
                .grantId("grt_" + UUID.randomUUID().toString().replace("-", ""))
                .appId(appId)
                .userId(userId)
                .region(region)
                .grantScopeType(grantScopeType)
                .deviceIds(join(deviceIds))
                .scopes(join(scopes))
                .status(STATUS_ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
        grantRepository.save(grant);
        return toResponse(grant);
    }

    @Override
    public List<GrantResponse> listGrants(OpenPlatformCallerContext callerContext) {
        String appId = callerContext.requireAppId();
        return grantRepository.listByAppId(appId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public GrantResponse getGrant(OpenPlatformCallerContext callerContext, String grantId) {
        return toResponse(requireOwnedGrant(callerContext.requireAppId(), grantId));
    }

    @Override
    public GrantResponse currentGrant(OpenPlatformCallerContext callerContext, String grantId) {
        if (StringUtils.hasText(grantId)) {
            return getGrant(callerContext, grantId);
        }
        if (!callerContext.isUserToken()) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.GRANT_ID_REQUIRED);
        }
        String now = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return GrantResponse.builder()
                .appId(callerContext.stableAppId())
                .userId(callerContext.stableUserId())
                .region(callerContext.currentRegion())
                .grantScopeType("all_devices")
                .deviceIds(Collections.emptyList())
                .status(STATUS_ACTIVE)
                .scopes(callerContext.currentScopes())
                .grantedAt(now)
                .updatedAt(now)
                .build();
    }

    @Override
    public GrantResponse revokeGrant(OpenPlatformCallerContext callerContext, String grantId) {
        String appId = callerContext.requireAppId();
        OpenPlatformUserGrant grant = requireOwnedGrant(appId, grantId);
        if (!STATUS_REVOKED.equals(grant.getStatus())) {
            LocalDateTime now = LocalDateTime.now();
            grantRepository.updateStatus(grantId, appId, STATUS_REVOKED, now, now);
            grant = grant.toBuilder()
                    .status(STATUS_REVOKED)
                    .revokedAt(now)
                    .updatedAt(now)
                    .build();
        }
        return toResponse(grant);
    }

    @Override
    public OpenPlatformUserGrant requireActiveGrant(String appId, String grantId) {
        OpenPlatformUserGrant grant = requireOwnedGrant(appId, grantId);
        if (!STATUS_ACTIVE.equals(grant.getStatus())) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.GRANT_NOT_ACTIVE);
        }
        return grant;
    }

    @Override
    public GrantResponse toResponse(OpenPlatformUserGrant grant) {
        if (grant == null) {
            return null;
        }
        return GrantResponse.builder()
                .grantId(grant.getGrantId())
                .appId(grant.getAppId())
                .userId(grant.getUserId())
                .region(grant.getRegion())
                .grantScopeType(grant.getGrantScopeType())
                .deviceIds(split(grant.getDeviceIds()))
                .status(grant.getStatus())
                .scopes(split(grant.getScopes()))
                .grantedAt(format(grant.getCreatedAt()))
                .updatedAt(format(grant.getUpdatedAt()))
                .revokedAt(format(grant.getRevokedAt()))
                .build();
    }

    private OpenPlatformUserGrant requireOwnedGrant(String appId, String grantId) {
        if (!StringUtils.hasText(grantId)) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.GRANT_ID_REQUIRED);
        }
        OpenPlatformUserGrant grant = grantRepository.findByGrantId(grantId.trim()).orElse(null);
        if (grant == null) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.GRANT_NOT_FOUND);
        }
        if (!appId.equals(grant.getAppId())) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.GRANT_APP_MISMATCH);
        }
        return grant;
    }

    private String normalizeGrantScopeType(String grantScopeType) {
        String normalized = StringUtils.hasText(grantScopeType) ? grantScopeType.trim() : "all_devices";
        if (!"all_devices".equals(normalized) && !"selected_devices".equals(normalized)) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.GRANT_SCOPE_TYPE_UNSUPPORTED);
        }
        return normalized;
    }

    private List<String> normalizeScopes(List<String> scopes) {
        List<String> normalized = scopes == null || scopes.isEmpty()
                ? requestContext.defaultScopes()
                : scopes.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toList());
        LinkedHashSet<String> deduplicated = new LinkedHashSet<>(normalized);
        if (deduplicated.isEmpty()) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.GRANT_SCOPES_REQUIRED);
        }
        for (String scope : deduplicated) {
            if (!SUPPORTED_GRANT_SCOPES.contains(scope)) {
                throw OpenPlatformDeveloperBizExceptions.of(
                        OpenPlatformDeveloperErrorCode.GRANT_SCOPE_UNSUPPORTED,
                        OpenPlatformDeveloperErrorCode.GRANT_SCOPE_UNSUPPORTED.getErrorMessage() + ": " + scope
                );
            }
        }
        return deduplicated.stream().collect(Collectors.toList());
    }

    private List<String> normalizeDeviceIds(List<String> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return Collections.emptyList();
        }
        return deviceIds.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    private void validateGrantScope(String grantScopeType, List<String> deviceIds) {
        if ("selected_devices".equals(grantScopeType) && (deviceIds == null || deviceIds.isEmpty())) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.GRANT_DEVICE_IDS_REQUIRED);
        }
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

    private String join(List<String> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return String.join(",", items);
    }

    private String format(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return time.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}

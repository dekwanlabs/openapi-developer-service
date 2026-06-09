package com.hesung.openapi.developer.service;

import com.hesung.openapi.developer.dao.OpenapiUserGrantDao;
import com.hesung.openapi.developer.dao.entity.OpenapiUserGrantEntity;
import com.hesung.openapi.developer.exception.OpenPlatformDeveloperBizExceptions;
import com.hesung.openapi.developer.exception.OpenPlatformDeveloperErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GrantService {

    private static final List<String> SUPPORTED_SCOPES = Collections.unmodifiableList(
            Arrays.asList("device.read", "device.control", "device.event.read"));

    private final OpenapiUserGrantDao grantDao;

    public GrantService(OpenapiUserGrantDao grantDao) {
        this.grantDao = grantDao;
    }

    // ── Create ──────────────────────────────────────────────────

    public OpenapiUserGrantEntity create(String appId, String userId, String region,
                                         String grantScopeType, List<String> deviceIds,
                                         List<String> scopes) {
        if (!StringUtils.hasText(appId)) {
            throw new IllegalArgumentException("appId is required");
        }
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("userId is required");
        }
        if (!StringUtils.hasText(grantScopeType)) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.GRANT_SCOPE_TYPE_UNSUPPORTED);
        }
        if (scopes == null || scopes.isEmpty()) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.GRANT_SCOPES_REQUIRED);
        }
        for (String scope : scopes) {
            if (!SUPPORTED_SCOPES.contains(scope)) {
                throw OpenPlatformDeveloperBizExceptions.of(
                        OpenPlatformDeveloperErrorCode.GRANT_SCOPE_UNSUPPORTED,
                        OpenPlatformDeveloperErrorCode.GRANT_SCOPE_UNSUPPORTED.getErrorMessage() + ": " + scope
                );
            }
        }
        if ("selected_devices".equals(grantScopeType)
                && (deviceIds == null || deviceIds.isEmpty())) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.GRANT_DEVICE_IDS_REQUIRED);
        }

        LocalDateTime now = LocalDateTime.now();
        OpenapiUserGrantEntity entity = new OpenapiUserGrantEntity();
        entity.setGrantId("grant_" + UUID.randomUUID().toString().replace("-", ""));
        entity.setAppId(appId);
        entity.setUserId(userId);
        entity.setRegion(region);
        entity.setGrantScopeType(grantScopeType);
        entity.setDeviceIds(deviceIds == null ? null : String.join(",", deviceIds));
        entity.setScopes(String.join(",", scopes));
        entity.setStatus("active");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        grantDao.insert(entity);
        return entity;
    }

    // ── Read ────────────────────────────────────────────────────

    public List<OpenapiUserGrantEntity> listByApp(String appId) {
        return grantDao.listByAppId(appId);
    }

    public OpenapiUserGrantEntity findByGrantId(String grantId) {
        OpenapiUserGrantEntity grant = grantDao.findByGrantId(grantId);
        if (grant == null) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.GRANT_NOT_FOUND);
        }
        return grant;
    }

    // ── Revoke ──────────────────────────────────────────────────

    public void revoke(String appId, String grantId) {
        OpenapiUserGrantEntity grant = requireActiveGrant(appId, grantId);
        LocalDateTime now = LocalDateTime.now();
        grantDao.updateStatus(grantId, appId, "revoked", now, now);
    }

    // ── Validate ────────────────────────────────────────────────

    /**
     * Validates that the caller (via grant) is authorized to access a device
     * with the given scope. Returns the grant on success, throws on failure.
     */
    public OpenapiUserGrantEntity validateAccess(String appId, String grantId,
                                                   String deviceId, String requiredScope) {
        OpenapiUserGrantEntity grant = requireActiveGrant(appId, grantId);

        List<String> grantScopes = split(grant.getScopes());
        if (StringUtils.hasText(requiredScope) && !grantScopes.contains(requiredScope)) {
            throw OpenPlatformDeveloperBizExceptions.of(
                    OpenPlatformDeveloperErrorCode.GRANT_SCOPE_MISSING,
                    OpenPlatformDeveloperErrorCode.GRANT_SCOPE_MISSING.getErrorMessage() + ": " + requiredScope
            );
        }

        if (StringUtils.hasText(deviceId)
                && "selected_devices".equals(grant.getGrantScopeType())) {
            List<String> allowed = split(grant.getDeviceIds());
            if (!allowed.contains(deviceId)) {
                throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.DEVICE_OUTSIDE_GRANT_SCOPE);
            }
        }

        return grant;
    }

    public OpenapiUserGrantEntity requireActiveGrant(String appId, String grantId) {
        OpenapiUserGrantEntity grant = findByGrantId(grantId);
        if (!"active".equals(grant.getStatus())) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.GRANT_NOT_ACTIVE);
        }
        if (StringUtils.hasText(appId) && !appId.equals(grant.getAppId())) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.GRANT_APP_MISMATCH);
        }
        return grant;
    }

    // ── Helpers ─────────────────────────────────────────────────

    private List<String> split(String csv) {
        if (!StringUtils.hasText(csv)) return Collections.emptyList();
        return Arrays.stream(csv.split(","))
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toList());
    }
}

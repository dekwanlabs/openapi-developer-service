package com.hesung.openapi.developer.infrastructure.persistence;

import com.hesung.openapi.developer.domain.model.OpenPlatformUserGrant;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OpenPlatformGrantRepository {

    Optional<OpenPlatformUserGrant> findByGrantId(String grantId);

    List<OpenPlatformUserGrant> listByAppId(String appId);

    OpenPlatformUserGrant save(OpenPlatformUserGrant grant);

    boolean updateStatus(String grantId, String appId, String status, LocalDateTime revokedAt, LocalDateTime updatedAt);
}

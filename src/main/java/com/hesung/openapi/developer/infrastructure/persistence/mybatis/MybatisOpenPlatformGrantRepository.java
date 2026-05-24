package com.hesung.openapi.developer.infrastructure.persistence.mybatis;

import com.hesung.openapi.developer.dao.OpenapiUserGrantDao;
import com.hesung.openapi.developer.dao.entity.OpenapiUserGrantEntity;
import com.hesung.openapi.developer.domain.model.OpenPlatformUserGrant;
import com.hesung.openapi.developer.infrastructure.persistence.OpenPlatformGrantRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class MybatisOpenPlatformGrantRepository implements OpenPlatformGrantRepository {

    private final OpenapiUserGrantDao openapiUserGrantDao;

    public MybatisOpenPlatformGrantRepository(OpenapiUserGrantDao openapiUserGrantDao) {
        this.openapiUserGrantDao = openapiUserGrantDao;
    }

    @Override
    public Optional<OpenPlatformUserGrant> findByGrantId(String grantId) {
        return Optional.ofNullable(openapiUserGrantDao.findByGrantId(grantId)).map(this::toGrant);
    }

    @Override
    public List<OpenPlatformUserGrant> listByAppId(String appId) {
        List<OpenapiUserGrantEntity> result = openapiUserGrantDao.listByAppId(appId);
        if (result == null || result.isEmpty()) {
            return Collections.emptyList();
        }
        return result.stream().map(this::toGrant).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public OpenPlatformUserGrant save(OpenPlatformUserGrant grant) {
        openapiUserGrantDao.insert(toEntity(grant));
        return grant;
    }

    @Override
    public boolean updateStatus(String grantId, String appId, String status, LocalDateTime revokedAt, LocalDateTime updatedAt) {
        return openapiUserGrantDao.updateStatus(grantId, appId, status, revokedAt, updatedAt) > 0;
    }

    private OpenPlatformUserGrant toGrant(OpenapiUserGrantEntity entity) {
        return OpenPlatformUserGrant.builder()
                .grantId(entity.getGrantId())
                .appId(entity.getAppId())
                .userId(entity.getUserId())
                .region(entity.getRegion())
                .grantScopeType(entity.getGrantScopeType())
                .deviceIds(entity.getDeviceIds())
                .scopes(entity.getScopes())
                .status(entity.getStatus())
                .revokedAt(entity.getRevokedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private OpenapiUserGrantEntity toEntity(OpenPlatformUserGrant grant) {
        OpenapiUserGrantEntity entity = new OpenapiUserGrantEntity();
        entity.setGrantId(grant.getGrantId());
        entity.setAppId(grant.getAppId());
        entity.setUserId(grant.getUserId());
        entity.setRegion(grant.getRegion());
        entity.setGrantScopeType(grant.getGrantScopeType());
        entity.setDeviceIds(grant.getDeviceIds());
        entity.setScopes(grant.getScopes());
        entity.setStatus(grant.getStatus());
        entity.setRevokedAt(grant.getRevokedAt());
        entity.setCreatedAt(grant.getCreatedAt());
        entity.setUpdatedAt(grant.getUpdatedAt());
        return entity;
    }
}

package com.hesung.openapi.developer.infrastructure.persistence.mybatis;

import com.hesung.openapi.developer.dao.OpenapiAppDao;
import com.hesung.openapi.developer.dao.entity.OpenapiAppEntity;
import com.hesung.openapi.developer.domain.model.OpenPlatformAppProfile;
import com.hesung.openapi.developer.infrastructure.persistence.OpenPlatformAppRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Primary
@Repository
public class MybatisOpenPlatformAppRepository implements OpenPlatformAppRepository {

    private final OpenapiAppDao openapiAppDao;

    public MybatisOpenPlatformAppRepository(OpenapiAppDao openapiAppDao) {
        this.openapiAppDao = openapiAppDao;
    }

    @Override
    public Optional<OpenPlatformAppProfile> findByClientId(String clientId) {
        return Optional.ofNullable(openapiAppDao.findByClientId(clientId)).map(this::toProfile);
    }

    private OpenPlatformAppProfile toProfile(OpenapiAppEntity entity) {
        return OpenPlatformAppProfile.builder()
                .appId(entity.getAppId())
                .clientId(entity.getClientId())
                .appName(entity.getAppName())
                .appType(entity.getAppType())
                .status(entity.getStatus())
                .build();
    }
}

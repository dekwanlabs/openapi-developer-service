package com.hesung.openapi.developer.infrastructure.persistence;

import com.hesung.openapi.developer.domain.model.OpenPlatformAppProfile;

import java.util.Optional;

public interface OpenPlatformAppRepository {

    Optional<OpenPlatformAppProfile> findByClientId(String clientId);
}

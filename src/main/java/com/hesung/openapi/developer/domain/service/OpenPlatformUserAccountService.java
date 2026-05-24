package com.hesung.openapi.developer.domain.service;

import com.hesung.openapi.developer.infrastructure.remote.user.dto.UserAccountInfo;

public interface OpenPlatformUserAccountService {

    UserAccountInfo resolveRequired(String userId, String userAccount);

    UserAccountInfo resolve(String userId, String userAccount);
}

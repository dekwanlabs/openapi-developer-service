package com.hesung.openapi.developer.domain.service.impl;

import com.hesung.openapi.developer.domain.service.OpenPlatformUserAccountService;
import com.hesung.openapi.developer.exception.OpenPlatformDeveloperBizExceptions;
import com.hesung.openapi.developer.exception.OpenPlatformDeveloperErrorCode;
import com.hesung.openapi.developer.infrastructure.remote.user.UserAccountClient;
import com.hesung.openapi.developer.infrastructure.remote.user.dto.UserAccountInfo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OpenPlatformUserAccountServiceImpl implements OpenPlatformUserAccountService {

    private final UserAccountClient userAccountClient;

    public OpenPlatformUserAccountServiceImpl(UserAccountClient userAccountClient) {
        this.userAccountClient = userAccountClient;
    }

    @Override
    public UserAccountInfo resolveRequired(String userId, String userAccount) {
        UserAccountInfo accountInfo = resolve(userId, userAccount);
        if (accountInfo == null || accountInfo.getId() == null || accountInfo.getId() <= 0) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.USER_ACCOUNT_NOT_FOUND);
        }
        return accountInfo;
    }

    @Override
    public UserAccountInfo resolve(String userId, String userAccount) {
        if (StringUtils.hasText(userId)) {
            try {
                return userAccountClient.getUserInfo(Long.parseLong(userId.trim()));
            } catch (NumberFormatException exception) {
                throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.USER_ID_INVALID);
            }
        }
        if (!StringUtils.hasText(userAccount)) {
            return null;
        }
        String normalized = userAccount.trim();
        if (normalized.matches("\\d+")) {
            return userAccountClient.getUserInfo(Long.parseLong(normalized));
        }
        return userAccountClient.getUserByEmail(normalized);
    }
}

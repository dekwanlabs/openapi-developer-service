package com.hesung.openapi.developer.exception;

import com.hesung.hsmf.enums.BaseBizExceptionEnum;

public enum OpenPlatformDeveloperErrorCode implements BaseBizExceptionEnum {

    APP_IDENTITY_NOT_FOUND(216001001, "App identity not found"),
    USER_ID_REQUIRED(216001002, "User ID is required"),
    USER_ID_INVALID(216001003, "User ID must be numeric"),
    INSUFFICIENT_SCOPE(216001004, "Insufficient scope"),
    USER_ACCOUNT_NOT_FOUND(216001005, "User account not found"),
    GRANT_ID_REQUIRED(216001006, "Grant ID is required"),
    GRANT_NOT_FOUND(216001007, "Grant not found"),
    GRANT_NOT_ACTIVE(216001008, "Grant is not active"),
    GRANT_APP_MISMATCH(216001009, "Grant does not belong to current app"),
    GRANT_SCOPE_TYPE_UNSUPPORTED(216001010, "Unsupported grant scope type"),
    GRANT_SCOPES_REQUIRED(216001011, "Scopes are required"),
    GRANT_SCOPE_UNSUPPORTED(216001012, "Unsupported scope"),
    GRANT_DEVICE_IDS_REQUIRED(216001013, "Device IDs are required for selected devices grant"),
    DEVICE_CAPABILITY_UNSUPPORTED(216001014, "Unsupported device capability"),
    DEVICE_COMMANDS_EMPTY(216001015, "No valid device commands"),
    DEVICE_NOT_FOUND(216001016, "Device not found"),
    GRANT_SCOPE_MISSING(216001017, "Grant does not include required scope"),
    DEVICE_OUTSIDE_GRANT_SCOPE(216001018, "Device is outside grant scope");

    private final Integer errorCode;
    private final String errorMessage;

    OpenPlatformDeveloperErrorCode(Integer errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public Integer getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}

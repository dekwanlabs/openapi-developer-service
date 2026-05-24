package com.hesung.openapi.developer.exception;

import com.hesung.hsmf.exception.BizException;

public final class OpenPlatformDeveloperBizExceptions {

    private OpenPlatformDeveloperBizExceptions() {
    }

    public static BizException of(OpenPlatformDeveloperErrorCode errorCode) {
        return new BizException(errorCode);
    }

    public static BizException of(OpenPlatformDeveloperErrorCode errorCode, String message) {
        return new BizException(errorCode.getErrorCode(), message);
    }
}

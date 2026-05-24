package com.hesung.openapi.developer.domain.service;

import com.hesung.openapi.developer.model.OpenPlatformCallerContext;
import com.hesung.openapi.developer.domain.model.OpenPlatformResolvedAccessContext;

public interface OpenPlatformAccessResolver {

    OpenPlatformResolvedAccessContext resolveDeviceAccess(OpenPlatformCallerContext callerContext,
                                                          String grantId,
                                                          String deviceId,
                                                          String requiredScope);
}

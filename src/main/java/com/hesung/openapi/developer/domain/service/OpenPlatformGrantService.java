package com.hesung.openapi.developer.domain.service;

import com.hesung.openapi.developer.controller.request.GrantCreateRequest;
import com.hesung.openapi.developer.controller.response.GrantResponse;
import com.hesung.openapi.developer.domain.model.OpenPlatformUserGrant;
import com.hesung.openapi.developer.model.OpenPlatformCallerContext;

import java.util.List;

public interface OpenPlatformGrantService {

    GrantResponse createGrant(OpenPlatformCallerContext callerContext, GrantCreateRequest createRequest);

    List<GrantResponse> listGrants(OpenPlatformCallerContext callerContext);

    GrantResponse getGrant(OpenPlatformCallerContext callerContext, String grantId);

    GrantResponse currentGrant(OpenPlatformCallerContext callerContext, String grantId);

    GrantResponse revokeGrant(OpenPlatformCallerContext callerContext, String grantId);

    OpenPlatformUserGrant requireActiveGrant(String appId, String grantId);

    GrantResponse toResponse(OpenPlatformUserGrant grant);
}

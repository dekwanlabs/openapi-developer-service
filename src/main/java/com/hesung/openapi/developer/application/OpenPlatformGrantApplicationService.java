package com.hesung.openapi.developer.application;

import com.hesung.openapi.developer.controller.request.GrantCreateRequest;
import com.hesung.openapi.developer.controller.response.GrantResponse;
import com.hesung.openapi.developer.domain.service.OpenPlatformGrantService;
import com.hesung.openapi.developer.model.OpenPlatformCallerContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenPlatformGrantApplicationService {

    private final OpenPlatformGrantService grantService;

    public OpenPlatformGrantApplicationService(OpenPlatformGrantService grantService) {
        this.grantService = grantService;
    }

    public GrantResponse createGrant(OpenPlatformCallerContext callerContext, GrantCreateRequest createRequest) {
        return grantService.createGrant(callerContext, createRequest);
    }

    public List<GrantResponse> listGrants(OpenPlatformCallerContext callerContext) {
        return grantService.listGrants(callerContext);
    }

    public GrantResponse getGrant(OpenPlatformCallerContext callerContext, String grantId) {
        return grantService.getGrant(callerContext, grantId);
    }

    public GrantResponse currentGrant(OpenPlatformCallerContext callerContext, String grantId) {
        return grantService.currentGrant(callerContext, grantId);
    }

    public GrantResponse revokeGrant(OpenPlatformCallerContext callerContext, String grantId) {
        return grantService.revokeGrant(callerContext, grantId);
    }
}

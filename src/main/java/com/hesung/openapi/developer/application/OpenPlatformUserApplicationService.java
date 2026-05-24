package com.hesung.openapi.developer.application;

import com.hesung.openapi.developer.model.OpenPlatformCallerContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class OpenPlatformUserApplicationService {

    public Map<String, Object> currentUser(OpenPlatformCallerContext callerContext) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", callerContext.stableUserId());
        payload.put("region", callerContext.currentRegion());
        payload.put("source", "hsmf-openapi-auth");
        return payload;
    }
}

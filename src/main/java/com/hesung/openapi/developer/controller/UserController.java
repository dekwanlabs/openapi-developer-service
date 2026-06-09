package com.hesung.openapi.developer.controller;

import com.hesung.openapi.developer.model.CallerContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Current authorized user info, resolved from the gateway-set
 * Application-Context header — no external call needed.
 */
@RestController
@RequestMapping("/open/users")
public class UserController {

    @GetMapping("/me")
    public Map<String, Object> currentUser(CallerContext caller) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("userId", caller.isUserToken() ? caller.getUserId() : null);
        user.put("appId", caller.getAppId());
        user.put("clientId", caller.getClientId());
        user.put("region", caller.currentRegion());
        user.put("scopes", caller.currentScopes());
        return user;
    }
}

package com.hesung.openapi.developer.controller;

import com.hesung.openapi.developer.controller.request.GrantCreateRequest;
import com.hesung.openapi.developer.controller.response.GrantResponse;
import com.hesung.openapi.developer.dao.entity.OpenapiUserGrantEntity;
import com.hesung.openapi.developer.model.CallerContext;
import com.hesung.openapi.developer.service.GrantService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User grant CRUD and access validation.
 * Grants represent a Dreo user's consent for a third-party app
 * to access their devices with specific scopes.
 */
@RestController
@RequestMapping("/open/grants")
public class GrantController {

    private final GrantService grantService;

    public GrantController(GrantService grantService) {
        this.grantService = grantService;
    }

    @PostMapping
    public GrantResponse create(CallerContext caller, @RequestBody GrantCreateRequest req) {
        OpenapiUserGrantEntity grant = grantService.create(
                caller.requireAppId(),
                req.getUserId(),
                req.getRegion(),
                req.getGrantScopeType(),
                req.getDeviceIds(),
                req.getScopes()
        );
        return GrantResponse.from(grant);
    }

    @GetMapping
    public List<GrantResponse> list(CallerContext caller) {
        return grantService.listByApp(caller.requireAppId()).stream()
                .map(GrantResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{grantId}")
    public GrantResponse get(CallerContext caller, @PathVariable String grantId) {
        OpenapiUserGrantEntity grant = grantService.requireActiveGrant(
                caller.requireAppId(), grantId);
        return GrantResponse.from(grant);
    }

    @DeleteMapping("/{grantId}")
    public void revoke(CallerContext caller, @PathVariable String grantId) {
        grantService.revoke(caller.requireAppId(), grantId);
    }
}

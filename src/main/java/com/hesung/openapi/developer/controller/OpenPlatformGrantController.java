package com.hesung.openapi.developer.controller;

import com.hesung.hsmf.annotation.ResultBodyWrapper;
import com.hesung.openapi.developer.application.OpenPlatformGrantApplicationService;
import com.hesung.openapi.developer.controller.request.GrantCreateRequest;
import com.hesung.openapi.developer.controller.response.GrantResponse;
import com.hesung.openapi.developer.model.OpenPlatformCallerContext;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 开放平台用户授权关系接口。
 */
@RestController
@RequestMapping("/open/grants")
@ResultBodyWrapper
public class OpenPlatformGrantController {

    private final OpenPlatformGrantApplicationService grantApplicationService;

    public OpenPlatformGrantController(OpenPlatformGrantApplicationService grantApplicationService) {
        this.grantApplicationService = grantApplicationService;
    }

    @PostMapping
    public GrantResponse createGrant(OpenPlatformCallerContext callerContext, @RequestBody GrantCreateRequest createRequest) {
        return grantApplicationService.createGrant(callerContext, createRequest);
    }

    @GetMapping
    public List<GrantResponse> listGrants(OpenPlatformCallerContext callerContext) {
        return grantApplicationService.listGrants(callerContext);
    }

    @GetMapping("/{grantId}")
    public GrantResponse grant(OpenPlatformCallerContext callerContext, @PathVariable("grantId") String grantId) {
        return grantApplicationService.getGrant(callerContext, grantId);
    }

    @GetMapping("/current")
    public GrantResponse currentGrant(OpenPlatformCallerContext callerContext,
                                      @RequestParam(value = "grantId", required = false) String grantId) {
        return grantApplicationService.currentGrant(callerContext, grantId);
    }

    @DeleteMapping("/{grantId}")
    public GrantResponse revokeGrant(OpenPlatformCallerContext callerContext, @PathVariable("grantId") String grantId) {
        return grantApplicationService.revokeGrant(callerContext, grantId);
    }
}

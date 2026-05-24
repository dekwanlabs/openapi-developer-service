package com.hesung.openapi.developer.controller;

import com.hesung.hsmf.annotation.ResultBodyWrapper;
import com.hesung.openapi.developer.application.OpenPlatformUserApplicationService;
import com.hesung.openapi.developer.model.OpenPlatformCallerContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * 开放平台当前授权用户接口。
 *
 * <p>该 Controller 面向第三方开发者提供“当前 token 属于哪个 Dreo 用户”的基础信息。
 * 它不负责登录或发 token；认证仍由 hsmf-openapi-auth 和网关链路处理。
 * 这里仅从请求上下文中读取已解析好的用户和区域信息，并转换成开放平台响应。</p>
 */
@RestController
@RequestMapping("/open/users")
@ResultBodyWrapper
public class OpenPlatformUserController {

    private final OpenPlatformUserApplicationService userApplicationService;

    public OpenPlatformUserController(OpenPlatformUserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    /**
     * 获取当前授权用户信息。
     *
     * <p>用于第三方在完成 OAuth 授权后确认用户身份、区域等基础上下文。</p>
     */
    @GetMapping("/me")
    public Map<String, Object> currentUser(OpenPlatformCallerContext callerContext) {
        return userApplicationService.currentUser(callerContext);
    }
}

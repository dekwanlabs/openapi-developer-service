package com.hesung.openapi.developer.infrastructure.remote.user;

import com.hesung.openapi.developer.infrastructure.remote.user.dto.UserAccountInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "hsds-user-provider", contextId = "openapi-api-user-provider")
public interface UserAccountClient {

    @GetMapping("/user/info")
    UserAccountInfo getUserByEmail(@RequestParam("email") String email);

    @GetMapping("/user/info/{userId}")
    UserAccountInfo getUserInfo(@PathVariable("userId") Long userId);
}

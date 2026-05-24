package com.hesung.openapi.developer.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class OpenPlatformWebMvcConfig implements WebMvcConfigurer {

    private final OpenPlatformCallerContextArgumentResolver callerContextArgumentResolver;

    public OpenPlatformWebMvcConfig(OpenPlatformCallerContextArgumentResolver callerContextArgumentResolver) {
        this.callerContextArgumentResolver = callerContextArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(callerContextArgumentResolver);
    }
}

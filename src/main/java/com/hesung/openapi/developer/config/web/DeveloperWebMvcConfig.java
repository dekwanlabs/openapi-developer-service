package com.hesung.openapi.developer.config.web;

import com.hesung.openapi.developer.model.CallerContextResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class DeveloperWebMvcConfig implements WebMvcConfigurer {

    private final CallerContextResolver callerContextResolver;

    public DeveloperWebMvcConfig(CallerContextResolver callerContextResolver) {
        this.callerContextResolver = callerContextResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(callerContextResolver);
    }
}

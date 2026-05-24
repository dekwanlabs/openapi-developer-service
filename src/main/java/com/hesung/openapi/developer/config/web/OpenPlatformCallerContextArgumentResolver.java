package com.hesung.openapi.developer.config.web;

import com.hesung.openapi.developer.model.OpenPlatformCallerContext;
import com.hesung.openapi.developer.infrastructure.web.OpenPlatformRequestContext;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

@Component
public class OpenPlatformCallerContextArgumentResolver implements HandlerMethodArgumentResolver {

    private final OpenPlatformRequestContext requestContext;

    public OpenPlatformCallerContextArgumentResolver(OpenPlatformRequestContext requestContext) {
        this.requestContext = requestContext;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return OpenPlatformCallerContext.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        return requestContext.resolve(request);
    }
}

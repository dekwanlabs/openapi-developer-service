package com.hesung.openapi.developer.model;

import com.hesung.hsmf.context.ApplicationContextProtobuf;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resolves {@link CallerContext} from the gateway-set Application-Context header.
 *
 * The header is a Protobuf + Base64 encoded payload set by
 * hsmf-openapi-gateway's AccessFilter after OAuth token validation.
 */
@Component
public class CallerContextResolver implements HandlerMethodArgumentResolver {

    private static final String HEADER_APPLICATION_CONTEXT = "Application-Context";
    private static final String ATTR_CACHED = CallerContextResolver.class.getName() + ".cached";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return CallerContext.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return CallerContext.builder().scopes(Collections.emptyList()).build();
        }

        CallerContext cached = (CallerContext) request.getAttribute(ATTR_CACHED);
        if (cached != null) {
            return cached;
        }

        CallerContext ctx = parse(request.getHeader(HEADER_APPLICATION_CONTEXT));
        request.setAttribute(ATTR_CACHED, ctx);
        return ctx;
    }

    private CallerContext parse(String headerValue) {
        if (!StringUtils.hasText(headerValue)) {
            return CallerContext.builder().scopes(Collections.emptyList()).build();
        }
        try {
            ApplicationContextProtobuf.ApplicationContext ctx =
                    ApplicationContextProtobuf.ApplicationContext.parseFrom(Base64Utils.decodeFromString(headerValue));

            String clientId = null;
            String appId = null;
            List<String> scopes = Collections.emptyList();
            if (ctx.hasClientDetail()) {
                clientId = emptyToNull(ctx.getClientDetail().getClientId());
                appId = emptyToNull(ctx.getClientDetail().getAppid());
                scopes = ctx.getClientDetail().getScopeList().stream()
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList());
            }

            String userId = null;
            String region = null;
            if (ctx.hasUserDetail() && ctx.getUserDetail().getUserid() > 0) {
                userId = String.valueOf(ctx.getUserDetail().getUserid());
                region = emptyToNull(ctx.getUserDetail().getRegion());
            }

            return CallerContext.builder()
                    .clientId(clientId)
                    .appId(appId)
                    .userId(userId)
                    .region(region)
                    .scopes(scopes)
                    .build();
        } catch (Exception e) {
            return CallerContext.builder().scopes(Collections.emptyList()).build();
        }
    }

    private String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

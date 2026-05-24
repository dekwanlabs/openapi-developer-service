package com.hesung.openapi.developer.infrastructure.web;

import com.hesung.hsmf.context.ApplicationContextProtobuf;
import com.hesung.openapi.developer.domain.model.OpenPlatformAppProfile;
import com.hesung.openapi.developer.exception.OpenPlatformDeveloperBizExceptions;
import com.hesung.openapi.developer.exception.OpenPlatformDeveloperErrorCode;
import com.hesung.openapi.developer.infrastructure.persistence.OpenPlatformAppRepository;
import com.hesung.openapi.developer.model.OpenPlatformCallerContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OpenPlatformRequestContext {

    private static final String REQUEST_ATTRIBUTE = OpenPlatformRequestContext.class.getName() + ".snapshot";

    private static final List<String> DEFAULT_SCOPES = Arrays.asList(
            OpenPlatformScope.DEVICE_READ.value(),
            OpenPlatformScope.DEVICE_CONTROL.value(),
            OpenPlatformScope.DEVICE_EVENT_READ.value()
    );

    private final OpenPlatformAppRepository appRepository;

    public OpenPlatformRequestContext(OpenPlatformAppRepository appRepository) {
        this.appRepository = appRepository;
    }

    public OpenPlatformCallerContext resolve(HttpServletRequest request) {
        ContextSnapshot snapshot = snapshot(request);
        String appId = snapshot.appId;
        if (!StringUtils.hasText(appId) && StringUtils.hasText(snapshot.clientId)) {
            OpenPlatformAppProfile appProfile = appRepository.findByClientId(snapshot.clientId).orElse(null);
            appId = appProfile == null ? null : appProfile.getAppId();
        }
        List<String> scopes = snapshot.scopes;
        if (!scopes.isEmpty()) {
            return OpenPlatformCallerContext.builder()
                    .clientId(snapshot.clientId)
                    .appId(appId)
                    .userId(snapshot.userId)
                    .region(snapshot.region)
                    .scopes(scopes)
                    .applicationContextHeader(snapshot.applicationContextHeader)
                    .build();
        }
        String scopeHeader = readHeader(request, OpenPlatformHeader.SCOPES);
        return OpenPlatformCallerContext.builder()
                .clientId(snapshot.clientId)
                .appId(appId)
                .userId(snapshot.userId)
                .region(snapshot.region)
                .scopes(StringUtils.hasText(scopeHeader)
                        ? normalizeList(Arrays.asList(scopeHeader.trim().split("[, ]+")))
                        : DEFAULT_SCOPES)
                .applicationContextHeader(snapshot.applicationContextHeader)
                .build();
    }

    public List<String> defaultScopes() {
        return Collections.unmodifiableList(DEFAULT_SCOPES);
    }

    public String buildUserApplicationContextHeader(OpenPlatformCallerContext callerContext, String userId, String region) {
        long userIdValue = parseUserId(userId);
        ApplicationContextProtobuf.ApplicationContext parsedContext =
                callerContext == null ? null : parseApplicationContext(callerContext.getApplicationContextHeader());

        ApplicationContextProtobuf.ApplicationContext.Builder builder =
                parsedContext == null
                        ? ApplicationContextProtobuf.ApplicationContext.newBuilder()
                        : parsedContext.toBuilder();
        if (!StringUtils.hasText(builder.getContextType())) {
            builder.setContextType("USER");
        }

        ApplicationContextProtobuf.ApplicationContext.ClientDetail.Builder clientDetailBuilder =
                builder.hasClientDetail()
                        ? builder.getClientDetail().toBuilder()
                        : ApplicationContextProtobuf.ApplicationContext.ClientDetail.newBuilder();
        if (callerContext != null && StringUtils.hasText(callerContext.getClientId())) {
            clientDetailBuilder.setClientId(callerContext.getClientId());
        }
        if (callerContext != null && StringUtils.hasText(callerContext.getAppId())) {
            clientDetailBuilder.setAppid(callerContext.getAppId());
        }
        if (callerContext != null && !callerContext.currentScopes().isEmpty()) {
            clientDetailBuilder.clearScope();
            clientDetailBuilder.addAllScope(callerContext.currentScopes());
        }
        builder.setClientDetail(clientDetailBuilder.build());

        ApplicationContextProtobuf.ApplicationContext.UserDetail.Builder userDetailBuilder =
                builder.hasUserDetail()
                        ? builder.getUserDetail().toBuilder()
                        : ApplicationContextProtobuf.ApplicationContext.UserDetail.newBuilder();
        userDetailBuilder.setUserid(userIdValue);
        if (StringUtils.hasText(region)) {
            userDetailBuilder.setRegion(region);
        }
        builder.setUserDetail(userDetailBuilder.build());
        return Base64Utils.encodeToString(builder.build().toByteArray());
    }

    private long parseUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.USER_ID_REQUIRED);
        }
        try {
            return Long.parseLong(userId.trim());
        } catch (NumberFormatException exception) {
            throw OpenPlatformDeveloperBizExceptions.of(OpenPlatformDeveloperErrorCode.USER_ID_INVALID);
        }
    }

    private ContextSnapshot snapshot(HttpServletRequest request) {
        if (request == null) {
            return ContextSnapshot.empty();
        }
        Object existing = request.getAttribute(REQUEST_ATTRIBUTE);
        if (existing instanceof ContextSnapshot) {
            return (ContextSnapshot) existing;
        }

        ContextSnapshot snapshot = parseSnapshot(request);
        request.setAttribute(REQUEST_ATTRIBUTE, snapshot);
        return snapshot;
    }

    private ContextSnapshot parseSnapshot(HttpServletRequest request) {
        String applicationContextHeader = readHeader(request, OpenPlatformHeader.APPLICATION_CONTEXT);
        ApplicationContextProtobuf.ApplicationContext applicationContext = parseApplicationContext(applicationContextHeader);

        String clientId = null;
        String appId = null;
        String userId = null;
        String region = null;
        List<String> scopes = Collections.emptyList();

        if (applicationContext != null) {
            if (applicationContext.hasClientDetail()) {
                clientId = emptyToNull(applicationContext.getClientDetail().getClientId());
                appId = emptyToNull(applicationContext.getClientDetail().getAppid());
                scopes = normalizeList(applicationContext.getClientDetail().getScopeList());
            }
            if (applicationContext.hasUserDetail() && applicationContext.getUserDetail().getUserid() > 0) {
                userId = String.valueOf(applicationContext.getUserDetail().getUserid());
                region = emptyToNull(applicationContext.getUserDetail().getRegion());
            }
        }

        clientId = StringUtils.hasText(clientId) ? clientId : readHeader(request, OpenPlatformHeader.CLIENT_ID);
        appId = StringUtils.hasText(appId) ? appId : readHeader(request, OpenPlatformHeader.APP_ID);
        userId = StringUtils.hasText(userId) ? userId : readHeader(request, OpenPlatformHeader.USER_ID);
        region = StringUtils.hasText(region) ? region : readHeader(request, OpenPlatformHeader.REGION);

        return new ContextSnapshot(applicationContextHeader, clientId, appId, userId, region, scopes);
    }

    private ApplicationContextProtobuf.ApplicationContext parseApplicationContext(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            return ApplicationContextProtobuf.ApplicationContext.parseFrom(Base64Utils.decodeFromString(token));
        } catch (Exception exception) {
            return null;
        }
    }

    private List<String> normalizeList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    private String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String readHeader(HttpServletRequest request, OpenPlatformHeader header) {
        if (request == null || header == null) {
            return null;
        }
        String value = request.getHeader(header.headerName());
        if (StringUtils.hasText(value)) {
            return value.trim();
        }
        return null;
    }

    private static final class ContextSnapshot {

        private final String applicationContextHeader;

        private final String clientId;

        private final String appId;

        private final String userId;

        private final String region;

        private final List<String> scopes;

        private ContextSnapshot(String applicationContextHeader,
                                String clientId,
                                String appId,
                                String userId,
                                String region,
                                List<String> scopes) {
            this.applicationContextHeader = applicationContextHeader;
            this.clientId = clientId;
            this.appId = appId;
            this.userId = userId;
            this.region = region;
            this.scopes = scopes == null ? Collections.emptyList() : scopes;
        }

        private static ContextSnapshot empty() {
            return new ContextSnapshot(null, null, null, null, null, Collections.emptyList());
        }
    }
}

package com.hesung.openapi.developer.infrastructure.web;

public enum OpenPlatformHeader {

    APPLICATION_CONTEXT("Application-Context"),
    CLIENT_ID("X-Client-Id"),
    APP_ID("X-App-Id"),
    USER_ID("X-User-Id"),
    REGION("X-Region"),
    SCOPES("X-Scopes");

    private final String headerName;

    OpenPlatformHeader(String headerName) {
        this.headerName = headerName;
    }

    public String headerName() {
        return headerName;
    }
}

package com.hesung.openapi.developer.infrastructure.web;

public enum OpenPlatformScope {

    DEVICE_READ("device.read"),
    DEVICE_CONTROL("device.control"),
    DEVICE_EVENT_READ("device.event.read");

    private final String value;

    OpenPlatformScope(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}

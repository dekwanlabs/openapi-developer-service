package com.hesung.openapi.developer.controller.request;

import lombok.Data;

import java.util.List;

@Data
public class GrantCreateRequest {

    /** Dreo user ID. Required. */
    private String userId;

    /** User region, e.g. "US". Optional, defaults to "US". */
    private String region;

    /** "all_devices" or "selected_devices". Required. */
    private String grantScopeType;

    /** Device IDs when grantScopeType = selected_devices. */
    private List<String> deviceIds;

    /** Scopes, e.g. ["device.read", "device.control"]. Required. */
    private List<String> scopes;
}

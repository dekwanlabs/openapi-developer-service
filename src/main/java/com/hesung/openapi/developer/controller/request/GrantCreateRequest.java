package com.hesung.openapi.developer.controller.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GrantCreateRequest {

    private String userId;

    private String userAccount;

    private String region;

    private String grantScopeType;

    private List<String> deviceIds = new ArrayList<>();

    private List<String> scopes = new ArrayList<>();
}

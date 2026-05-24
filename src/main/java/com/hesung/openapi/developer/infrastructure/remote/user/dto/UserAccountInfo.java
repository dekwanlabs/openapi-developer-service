package com.hesung.openapi.developer.infrastructure.remote.user.dto;

import lombok.Data;

@Data
public class UserAccountInfo {

    private Long id;

    private String nickname;

    private String email;

    private boolean verified;
}

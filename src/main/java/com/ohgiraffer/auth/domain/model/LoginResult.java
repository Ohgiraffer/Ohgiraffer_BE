package com.ohgiraffer.auth.domain.model;

import com.ohgiraffer.auth.presentation.api.response.LoginResponse;

import java.time.LocalDateTime;

public record LoginResult(
        LoginResponse body,
        String refreshToken,
        LocalDateTime refreshTokenExpiresAt
) {
}
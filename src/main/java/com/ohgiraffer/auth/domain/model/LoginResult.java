package com.ohgiraffer.auth.domain.model;

import com.ohgiraffer.auth.presentation.api.response.LoginResponse;

public record LoginResult(
        LoginResponse body,
        String refreshToken
) {
}

package com.ohgiraffer.auth.application.usecase;

import com.ohgiraffer.auth.domain.model.LoginResult;
import com.ohgiraffer.auth.presentation.api.request.LoginRequest;
import com.ohgiraffer.auth.presentation.api.response.TokenResponse;

public interface AuthCommandUsecase {

    LoginResult login(LoginRequest request, String clientIp);

    void logout(Long id, String bearerToken, String refreshToken);

    TokenResponse reissueAccessToken(String refreshToken);
}

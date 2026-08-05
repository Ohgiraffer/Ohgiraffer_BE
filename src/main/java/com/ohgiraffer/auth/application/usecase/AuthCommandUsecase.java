package com.ohgiraffer.auth.application.usecase;

import com.ohgiraffer.auth.domain.model.LoginResult;
import com.ohgiraffer.auth.presentation.api.request.LoginRequest;

public interface AuthCommandUsecase {

    LoginResult login(LoginRequest request, String clientIp);

    void logout(Long id, String bearerToken, String refreshToken);

    String reissueAccessToken(String refreshToken);
}

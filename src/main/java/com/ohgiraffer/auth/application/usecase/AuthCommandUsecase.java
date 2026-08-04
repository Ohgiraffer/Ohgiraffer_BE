package com.ohgiraffer.auth.application.usecase;

import com.ohgiraffer.auth.presentation.api.request.LoginRequest;
import com.ohgiraffer.auth.presentation.api.response.LoginResponse;

public interface AuthCommandUsecase {

    LoginResponse login(LoginRequest request, String clientIp);
}

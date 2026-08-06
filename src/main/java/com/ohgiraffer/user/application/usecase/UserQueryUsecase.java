package com.ohgiraffer.user.application.usecase;

import com.ohgiraffer.user.presentation.api.response.UserResponse;

public interface UserQueryUsecase {
    UserResponse getMyInfo(Long userId);
}

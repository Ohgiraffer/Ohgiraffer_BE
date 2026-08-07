package com.ohgiraffer.user.application.usecase;

import com.ohgiraffer.user.presentation.api.response.UserResponse;
import com.ohgiraffer.user.presentation.api.response.UserSheetConnectionResponse;

public interface UserQueryUsecase {
    UserResponse getMyInfo(Long userId);

    UserSheetConnectionResponse checkSheetConnection(String spreadsheetUrl);
}

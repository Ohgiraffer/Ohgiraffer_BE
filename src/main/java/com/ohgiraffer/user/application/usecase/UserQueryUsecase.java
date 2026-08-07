package com.ohgiraffer.user.application.usecase;

import com.ohgiraffer.user.presentation.api.response.UserResponse;
import com.ohgiraffer.user.presentation.api.response.UserSheetConnectionResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserQueryUsecase {
    UserResponse getMyInfo(Long userId);

    UserSheetConnectionResponse checkFileConnection(MultipartFile file);

    Long getBootcampId(Long userId);
}

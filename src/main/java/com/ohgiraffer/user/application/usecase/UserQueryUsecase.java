package com.ohgiraffer.user.application.usecase;

import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.presentation.api.response.UserResponse;
import com.ohgiraffer.user.presentation.api.response.UserSheetConnectionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserQueryUsecase {
    UserResponse getMyInfo(Long userId);

    UserSheetConnectionResponse checkFileConnection(MultipartFile file);

    Long getBootcampId(Long userId);

    Role getRole(Long userId);

    List<Long> getStudentIdsByBootcampId(Long bootcampId);
}

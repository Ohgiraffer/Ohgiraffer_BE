package com.ohgiraffer.user.application.usecase;

import com.ohgiraffer.user.domain.model.UserStatus;
import com.ohgiraffer.user.presentation.api.request.AddUserRequest;

import org.springframework.web.multipart.MultipartFile;

public interface UserCommandUsecase {

    void changePassword(Long userId, String bearerToken, String newPassword);

    boolean setAlarm(Long id);

    String updateProfileImg(Long userId, MultipartFile profileImg);

    void deleteProfileImg(Long id);

    void changeUserStatus(Long userId, UserStatus status);

    void addUsers(AddUserRequest request, Long requesterId);
}

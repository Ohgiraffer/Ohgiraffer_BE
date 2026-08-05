package com.ohgiraffer.user.application.usecase;

import org.springframework.web.multipart.MultipartFile;

public interface UserCommandUsecase {

    void changePassword(Long userId, String bearerToken, String newPassword);

    boolean setAlarm(Long id);

    String updateProfileImg(Long userId, MultipartFile profileImg);

    void deleteProfileImg(Long id);
}

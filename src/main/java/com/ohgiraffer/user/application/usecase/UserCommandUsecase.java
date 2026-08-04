package com.ohgiraffer.user.application.usecase;

public interface UserCommandUsecase {

    void changePassword(Long userId, String bearerToken, String newPassword);

    boolean setAlarm(Long id);
}

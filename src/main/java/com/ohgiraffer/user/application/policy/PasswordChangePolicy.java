package com.ohgiraffer.user.application.policy;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.domain.model.User;

public class PasswordChangePolicy {

    private PasswordChangePolicy() {}

    public static void validateResettable(User user) {
        if (!user.isNeedResetPw()) {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_NOT_REQUIRED);
        }
    }
}
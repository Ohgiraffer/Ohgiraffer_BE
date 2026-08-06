package com.ohgiraffer.auth.application.policy;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class LogoutPolicy {

    private static final String BEARER_PREFIX = "Bearer ";

    public static String resolveAccessToken(String bearerToken) {
        if (bearerToken == null) {
            throw new BusinessException(ErrorCode.MISSING_ACCESS_TOKEN);
        }
        if (!bearerToken.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return bearerToken.substring(BEARER_PREFIX.length());
    }

    public void validateRefreshToken(String refreshToken) {
        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.MISSING_REFRESH_TOKEN);
        }
    }
}
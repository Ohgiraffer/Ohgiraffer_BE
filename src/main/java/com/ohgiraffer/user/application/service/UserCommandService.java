package com.ohgiraffer.user.application.service;

import com.ohgiraffer.auth.application.policy.LogoutPolicy;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.security.jwt.JwtTokenProvider;
import com.ohgiraffer.security.token.RefreshTokenService;
import com.ohgiraffer.security.token.TokenBlacklistService;
import com.ohgiraffer.user.application.policy.PasswordChangePolicy;
import com.ohgiraffer.user.application.usecase.UserCommandUsecase;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UserCommandService implements UserCommandUsecase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void changePassword(Long userId, String bearerToken, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        PasswordChangePolicy.validateResettable(user);

        user.changePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        invalidateCurrentSession(userId, bearerToken);

        log.info("[changePassword] 최초 비밀번호 변경 완료 | userId={}", userId);
    }

    private void invalidateCurrentSession(Long userId, String bearerToken) {
        String accessToken = LogoutPolicy.resolveAccessToken(bearerToken);

        Claims claims = jwtTokenProvider.resolveAccessClaims(accessToken);
        if (claims != null) {
            String jti = jwtTokenProvider.extractJti(claims);
            long remainingMs = jwtTokenProvider.getRemainingValidityMs(claims);
            tokenBlacklistService.blacklist(jti, remainingMs);
        }
        refreshTokenService.delete(userId);
    }

    @Override
    public boolean setAlarm(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setAlarm();
        userRepository.save(user);

        log.info("[setAlarm] 알림 설정 변경 완료 | userId={}, notificationOn={}", userId, user.isNotificationOn());
        return user.isNotificationOn();
    }
}

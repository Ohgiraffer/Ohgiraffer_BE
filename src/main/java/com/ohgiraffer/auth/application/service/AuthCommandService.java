package com.ohgiraffer.auth.application.service;

import com.ohgiraffer.auth.application.policy.LogoutPolicy;
import com.ohgiraffer.auth.application.usecase.AuthCommandUsecase;
import com.ohgiraffer.auth.domain.event.UserLoggedInEvent;
import com.ohgiraffer.auth.domain.model.LoginResult;
import com.ohgiraffer.auth.presentation.api.request.LoginRequest;
import com.ohgiraffer.auth.presentation.api.response.LoginResponse;
import com.ohgiraffer.auth.presentation.api.response.TokenResponse;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.security.FailureCountGuard;
import com.ohgiraffer.security.jwt.JwtTokenProvider;
import com.ohgiraffer.security.token.RefreshTokenService;
import com.ohgiraffer.security.token.TokenBlacklistService;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.model.UserStatus;
import com.ohgiraffer.user.domain.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AuthCommandService implements AuthCommandUsecase {

    private static final String LOGIN_FAILURE_SCOPE = "login";
    private static final int LOGIN_MAX_FAILURE_COUNT = 5;
    private static final Duration LOGIN_FAILURE_WINDOW = Duration.ofMinutes(10);
    private static final Duration LOGIN_LOCK_DURATION = Duration.ofMinutes(15);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final LogoutPolicy logoutPolicy;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final ApplicationEventPublisher eventPublisher;
    private final FailureCountGuard failureCountGuard;

    @Override
    public LoginResult login(LoginRequest request, String clientIp) {

        failureCountGuard.checkNotLocked(LOGIN_FAILURE_SCOPE, request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("[login] 존재하지 않는 이메일로 로그인 시도 | email={} | ip={}",
                            maskEmail(request.email()), maskIp(clientIp));
                    failureCountGuard.recordFailure(LOGIN_FAILURE_SCOPE, request.email(),
                            LOGIN_MAX_FAILURE_COUNT, LOGIN_FAILURE_WINDOW, LOGIN_LOCK_DURATION);
                    return new BusinessException(ErrorCode.LOGIN_FAILED);
                });

        if (user.getStatus() != UserStatus.ACTIVE && user.getStatus() != UserStatus.COMPLETED) {
            log.warn("[login] 비활성 계정 로그인 시도 | email={} | status={}",
                    maskEmail(request.email()), user.getStatus());
            ErrorCode errorCode = switch (user.getStatus()) {
                case WITHDRAWN -> ErrorCode.WITHDRAWN_MEMBER;
                case EXPELLED -> ErrorCode.EXPELLED_MEMBER;
                default -> ErrorCode.FORBIDDEN;
            };
            throw new BusinessException(errorCode);
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException | DisabledException | LockedException e) {
            log.warn("[login] 인증 실패 | email={} | ip={} | reason={}",
                    maskEmail(request.email()), maskIp(clientIp), e.getClass().getSimpleName());
            failureCountGuard.recordFailure(LOGIN_FAILURE_SCOPE, request.email(),
                    LOGIN_MAX_FAILURE_COUNT, LOGIN_FAILURE_WINDOW, LOGIN_LOCK_DURATION);
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        failureCountGuard.resetFailure(LOGIN_FAILURE_SCOPE, request.email());

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();

        eventPublisher.publishEvent(new UserLoggedInEvent(principal.getId(), user.getName(), user.getProfileImg()));

        String accessToken = jwtTokenProvider.createAccessToken(principal.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(principal.getId());

        LocalDateTime refreshTokenExpiresAt = LocalDate.now().plusDays(1).atStartOfDay();

        refreshTokenService.save(
                principal.getId(),
                refreshToken,
                Duration.between(LocalDateTime.now(), refreshTokenExpiresAt)
        );

        return new LoginResult(
                LoginResponse.of(accessToken, user.getRole(), user.getStatus(), user.getBootcampId(), user.isNeedResetPw()),
                refreshToken,
                refreshTokenExpiresAt
        );
    }

    @Override
    public void logout(Long id, String bearerToken, String refreshToken) {
        String accessToken = logoutPolicy.resolveAccessToken(bearerToken);
        logoutPolicy.validateRefreshToken(refreshToken);

        if (!refreshTokenService.isValid(id, refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Claims claims = jwtTokenProvider.resolveAccessClaims(accessToken);
        if (claims != null) {
            String jti = jwtTokenProvider.extractJti(claims);

            long remainingMs = jwtTokenProvider.getRemainingValidityMs(claims);
            tokenBlacklistService.blacklist(jti, remainingMs);
        }

        refreshTokenService.delete(id);
        log.info("[logout] 로그아웃 처리 완료 | userId={}", id);
    }

    @Override
    public TokenResponse reissueAccessToken(String refreshToken) {
        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.MISSING_REFRESH_TOKEN);
        }

        Claims claims = jwtTokenProvider.resolveRefreshClaims(refreshToken);
        if (claims == null) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtTokenProvider.extractUserId(claims);

        if (!refreshTokenService.isValid(userId, refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtTokenProvider.createAccessToken(userId);

        return new TokenResponse(user.getId(), newAccessToken, user.getRole(), user.getStatus(), user.getBootcampId());
    }

    private String maskEmail(String email) {
        if (email == null) return null;
        int at = email.indexOf('@');
        if (at <= 2) return "***" + email.substring(Math.max(at, 0));
        return email.substring(0, 2) + "***" + email.substring(at);
    }

    private String maskIp(String ip) {
        if (ip == null) return null;
        int lastDot = ip.lastIndexOf('.');
        if (lastDot < 0) return "***";
        return ip.substring(0, lastDot) + ".*";
    }
}
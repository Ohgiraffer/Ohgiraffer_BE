package com.ohgiraffer.auth.application.service;

import com.ohgiraffer.auth.application.policy.LogoutPolicy;
import com.ohgiraffer.auth.application.usecase.AuthCommandUsecase;
import com.ohgiraffer.auth.domain.model.LoginResult;
import com.ohgiraffer.auth.presentation.api.request.LoginRequest;
import com.ohgiraffer.auth.presentation.api.response.LoginResponse;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
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

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final LogoutPolicy logoutPolicy;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public LoginResult login(LoginRequest request, String clientIp) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(()->new BusinessException(ErrorCode.LOGIN_FAILED));

        if (user.getStatus() != UserStatus.ACTIVE && user.getStatus() != UserStatus.COMPLETED) {
            log.warn("[login] 비활성 계정 로그인 시도 | email={} | status={}", request.email(), user.getStatus());
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
                    request.email(), clientIp, e.getClass().getSimpleName());
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.createAccessToken(principal.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(principal.getId());

        refreshTokenService.save(
                principal.getId(),
                refreshToken,
                Duration.between(LocalDateTime.now(), LocalDate.now().plusDays(1).atStartOfDay())
        );

        return new LoginResult(
                LoginResponse.of(accessToken, user.getRole(), user.getStatus()),
                refreshToken
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
    public String reissueAccessToken(String refreshToken) {
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

        return jwtTokenProvider.createAccessToken(userId);
    }
}

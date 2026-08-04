package com.ohgiraffer.auth.application.service;

import com.ohgiraffer.auth.application.usecase.AuthCommandUsecase;
import com.ohgiraffer.auth.presentation.api.request.LoginRequest;
import com.ohgiraffer.auth.presentation.api.response.LoginResponse;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.security.jwt.JwtTokenProvider;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.model.UserStatus;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AuthCommandService implements AuthCommandUsecase {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public LoginResponse login(LoginRequest request, String clientIp) {

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

        return LoginResponse.of(accessToken, refreshToken, user.getRole(), user.getStatus());
    }
}

package com.ohgiraffer.user.application.service;

import com.ohgiraffer.attendance.application.usecase.AttendanceCacheEvictUsecase;
import com.ohgiraffer.auth.application.policy.LogoutPolicy;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.s3.S3KeyGenerator;
import com.ohgiraffer.global.s3.S3FileHandler;
import com.ohgiraffer.global.s3.S3UrlResolver;
import com.ohgiraffer.security.jwt.JwtTokenProvider;
import com.ohgiraffer.security.token.RefreshTokenService;
import com.ohgiraffer.security.token.TokenBlacklistService;
import com.ohgiraffer.user.application.helper.UserProfileImgTransactionHelper;
import com.ohgiraffer.user.application.policy.PasswordChangePolicy;
import com.ohgiraffer.user.application.policy.ProfileImgChangePolicy;
import com.ohgiraffer.user.application.usecase.UserCommandUsecase;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.model.UserStatus;
import com.ohgiraffer.user.domain.repository.UserRepository;
import com.ohgiraffer.user.presentation.api.request.AddUserRequest;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserCommandService implements UserCommandUsecase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;
    private final S3FileHandler s3FileHandler;
    private final S3UrlResolver s3UrlResolver;
    private final UserProfileImgTransactionHelper transactionHelper;
    private final AttendanceCacheEvictUsecase attendanceCacheEvictUsecase;

    @Override
    @Transactional
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
    @Transactional
    public boolean setAlarm(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setAlarm();
        userRepository.save(user);

        log.info("[setAlarm] 알림 설정 변경 완료 | userId={}, notificationOn={}", userId, user.isNotificationOn());
        return user.isNotificationOn();
    }

    @Override
    public String updateProfileImg(Long userId, MultipartFile profileImg) {
        ProfileImgChangePolicy.validate(profileImg);

        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String key = S3KeyGenerator.profileImageKey(userId);

        transactionHelper.updateUserProfileImg(userId, key);
        s3FileHandler.upload(profileImg, key);

        log.info("[updateProfileImage] 프로필 이미지 변경 완료 | userId={}, key={}", userId, key);
        return s3UrlResolver.resolve(key);
    }

    @Override
    public void deleteProfileImg(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String key = user.getProfileImg();

        transactionHelper.deleteUserProfileImg(userId);
        s3FileHandler.delete(key);

        log.info("[deleteProfileImage] 프로필 이미지 삭제 완료 | userId={}", userId);
    }

    @Override
    @Transactional
    public void changeUserStatus(Long userId, UserStatus newStatus) {
        if (newStatus != UserStatus.WITHDRAWN && newStatus != UserStatus.EXPELLED) {
            throw new BusinessException(ErrorCode.INVALID_USER_STATUS_TARGET);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.dismiss(newStatus);
        userRepository.save(user);

        attendanceCacheEvictUsecase.evictAllForBootcamp(user.getBootcampId());
    }

    @Override
    @Transactional
    public void addUsers(AddUserRequest request, Long requesterId) {
        Long bootcampId = userRepository.findBootcampIdByUserId(requesterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<User> users = request.rows().stream()
                .map(row -> User.register(
                        row.name(),
                        row.phone(),
                        row.email(),
                        Role.fromSheetDisplayName(row.role()),
                        bootcampId
                ))
                .toList();

        try {
            userRepository.saveAll(users);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.USER_BULK_INSERT_FAILED);
        }

        attendanceCacheEvictUsecase.evictAllForBootcamp(bootcampId);
    }
}

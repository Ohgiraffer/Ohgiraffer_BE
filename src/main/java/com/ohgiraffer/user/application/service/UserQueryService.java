package com.ohgiraffer.user.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.s3.S3UrlResolver;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import com.ohgiraffer.user.presentation.api.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService implements UserQueryUsecase {

    private final UserRepository userRepository;
    private final S3UrlResolver s3UrlResolver;

    @Override
    public UserResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String profileImgUrl = user.getProfileImg() != null
                ? s3UrlResolver.resolve(user.getProfileImg())
                : null;

        return UserResponse.from(user, profileImgUrl);
    }
}


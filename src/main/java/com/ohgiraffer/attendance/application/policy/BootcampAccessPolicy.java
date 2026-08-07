package com.ohgiraffer.attendance.application.policy;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BootcampAccessPolicy {

    private final UserQueryUsecase userQueryUsecase;

    public void validateSameBootcamp(Long requesterId, Long targetUserId) {
        Long requesterBootcampId = userQueryUsecase.getBootcampId(requesterId);
        Long targetBootcampId = userQueryUsecase.getBootcampId(targetUserId);

        if (!requesterBootcampId.equals(targetBootcampId)) {
            throw new BusinessException(ErrorCode.BOOTCAMP_ACCESS_DENIED);
        }
    }
}
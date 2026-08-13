package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.query.ApprovalProfileResult;
import com.ohgiraffer.approval.application.usecase.GetMyApprovalProfileUseCase;
import com.ohgiraffer.approval.domain.repository.ApprovalApplicantProfileRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyApprovalProfileService
        implements GetMyApprovalProfileUseCase {

    private final UserRepository userRepository;
    private final ApprovalApplicantProfileRepository approvalApplicantProfileRepository;

    @Override
    public ApprovalProfileResult getProfile(
            Long loginUserId
    ) {
        User user = findUser(
                loginUserId
        );

        LocalDate birthDate =
                approvalApplicantProfileRepository.findByUserId(
                                loginUserId
                        )
                        .map(
                                profile -> profile.getBirthDate()
                        )
                        .orElse(
                                null
                        );

        return new ApprovalProfileResult(
                birthDate,
                user.getPhone()
        );
    }

    private User findUser(
            Long userId
    ) {
        return userRepository.findById(
                        userId
                )
                .orElseThrow(
                        () -> new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );
    }
}
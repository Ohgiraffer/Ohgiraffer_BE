package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.query.ApprovalProfileResult;
import com.ohgiraffer.approval.application.usecase.UpdateMyApprovalProfileUseCase;
import com.ohgiraffer.approval.domain.model.profile.ApprovalApplicantProfile;
import com.ohgiraffer.approval.domain.repository.ApprovalApplicantProfileRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateMyApprovalProfileService
        implements UpdateMyApprovalProfileUseCase {

    private final UserRepository userRepository;
    private final ApprovalApplicantProfileRepository approvalApplicantProfileRepository;
    private final Clock clock;

    @Override
    public ApprovalProfileResult updateProfile(
            Long loginUserId,
            LocalDate birthDate
    ) {
        User user = findUser(
                loginUserId
        );

        validateBirthDate(
                birthDate
        );

        ApprovalApplicantProfile savedProfile =
                saveOrUpdateProfile(
                        loginUserId,
                        birthDate
                );

        return new ApprovalProfileResult(
                savedProfile.getBirthDate(),
                user.getPhone()
        );
    }

    private ApprovalApplicantProfile saveOrUpdateProfile(
            Long loginUserId,
            LocalDate birthDate
    ) {
        LocalDateTime now =
                LocalDateTime.now(
                        clock
                );

        return approvalApplicantProfileRepository.findByUserId(
                        loginUserId
                )
                .map(
                        existingProfile -> updateExistingProfile(
                                existingProfile,
                                birthDate,
                                now
                        )
                )
                .orElseGet(
                        () -> createProfileSafely(
                                loginUserId,
                                birthDate,
                                now
                        )
                );
    }

    private ApprovalApplicantProfile updateExistingProfile(
            ApprovalApplicantProfile existingProfile,
            LocalDate birthDate,
            LocalDateTime now
    ) {
        existingProfile.updateBirthDate(
                birthDate,
                now
        );

        return approvalApplicantProfileRepository.save(
                existingProfile
        );
    }

    private ApprovalApplicantProfile createProfileSafely(
            Long loginUserId,
            LocalDate birthDate,
            LocalDateTime now
    ) {
        try {
            ApprovalApplicantProfile profile =
                    ApprovalApplicantProfile.create(
                            loginUserId,
                            birthDate,
                            now
                    );

            return approvalApplicantProfileRepository.save(
                    profile
            );
        } catch (DataIntegrityViolationException exception) {
            return retryUpdateAfterDuplicateProfile(
                    loginUserId,
                    birthDate,
                    now
            );
        }
    }

    private ApprovalApplicantProfile retryUpdateAfterDuplicateProfile(
            Long loginUserId,
            LocalDate birthDate,
            LocalDateTime now
    ) {
        ApprovalApplicantProfile existingProfile =
                approvalApplicantProfileRepository.findByUserId(
                                loginUserId
                        )
                        .orElseThrow(
                                () -> new BusinessException(
                                        ErrorCode.INTERNAL_SERVER_ERROR
                                )
                        );

        existingProfile.updateBirthDate(
                birthDate,
                now
        );

        return approvalApplicantProfileRepository.save(
                existingProfile
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

    private void validateBirthDate(
            LocalDate birthDate
    ) {
        if (birthDate == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        LocalDate today =
                LocalDate.now(
                        clock
                );

        if (birthDate.isAfter(
                today
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }
}
package com.ohgiraffer.approval.domain.model.profile;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApprovalApplicantProfile {

    private final Long id;
    private final Long userId;
    private LocalDate birthDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ApprovalApplicantProfile create(
            Long userId,
            LocalDate birthDate,
            LocalDateTime now
    ) {
        ApprovalApplicantProfile profile =
                new ApprovalApplicantProfile(
                        null,
                        userId
                );

        profile.updateBirthDate(
                birthDate,
                now
        );

        profile.createdAt = now;

        return profile;
    }

    public static ApprovalApplicantProfile restore(
            Long id,
            Long userId,
            LocalDate birthDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        ApprovalApplicantProfile profile =
                new ApprovalApplicantProfile(
                        id,
                        userId
                );

        profile.birthDate = birthDate;
        profile.createdAt = createdAt;
        profile.updatedAt = updatedAt;

        return profile;
    }

    public void updateBirthDate(
            LocalDate birthDate,
            LocalDateTime now
    ) {
        if (birthDate == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        this.birthDate = birthDate;
        this.updatedAt = now;
    }
}
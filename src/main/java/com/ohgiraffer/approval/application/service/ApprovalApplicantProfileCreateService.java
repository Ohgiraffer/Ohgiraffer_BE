package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.domain.model.profile.ApprovalApplicantProfile;
import com.ohgiraffer.approval.domain.repository.ApprovalApplicantProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ApprovalApplicantProfileCreateService {

    private final ApprovalApplicantProfileRepository approvalApplicantProfileRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ApprovalApplicantProfile create(
            Long userId,
            LocalDate birthDate,
            LocalDateTime now
    ) {
        ApprovalApplicantProfile profile =
                ApprovalApplicantProfile.create(
                        userId,
                        birthDate,
                        now
                );

        return approvalApplicantProfileRepository.save(
                profile
        );
    }
}
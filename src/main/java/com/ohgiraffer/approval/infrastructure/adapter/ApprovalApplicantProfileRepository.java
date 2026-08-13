package com.ohgiraffer.approval.infrastructure.adapter;

import com.ohgiraffer.approval.domain.model.profile.ApprovalApplicantProfile;

import java.util.Optional;

public interface ApprovalApplicantProfileRepository {

    ApprovalApplicantProfile save(
            ApprovalApplicantProfile approvalApplicantProfile
    );

    Optional<ApprovalApplicantProfile> findByUserId(
            Long userId
    );
}
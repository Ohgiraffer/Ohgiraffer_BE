package com.ohgiraffer.approval.domain.repository;

import com.ohgiraffer.approval.domain.model.approval.ApprovalRequest;

import java.util.Optional;

public interface ApprovalRequestRepository {

    ApprovalRequest save(
            ApprovalRequest approvalRequest
    );

    Optional<ApprovalRequest> findById(
            Long approvalId
    );
}
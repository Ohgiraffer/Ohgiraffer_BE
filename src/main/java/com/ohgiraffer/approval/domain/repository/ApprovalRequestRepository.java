package com.ohgiraffer.approval.domain.repository;

import com.ohgiraffer.approval.domain.model.approval.ApprovalRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ApprovalRequestRepository {

    ApprovalRequest save(
            ApprovalRequest approvalRequest
    );

    Optional<ApprovalRequest> findById(
            Long approvalId
    );

    List<ApprovalRequest> findByRequesterIdOrderByRequestedAtDesc(
            Long requesterId
    );

    List<ApprovalRequest> findProcessingApprovals(
            Long userId,
            Long bootcampId
    );

    int checkPendingApproval(
            Long approvalId,
            Long approverId,
            LocalDateTime confirmedAt
    );
}
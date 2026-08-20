package com.ohgiraffer.approval.domain.repository;

import com.ohgiraffer.approval.domain.model.approval.ApprovalRequest;
import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;
import com.ohgiraffer.approval.domain.model.approval.ApprovalType;

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

    List<ApprovalRequest> findManagerProcessingApprovals(
            Long bootcampId
    );

    List<ApprovalRequest> findByRequesterIdAndRequestTypeAndStatusInOrderByRequestedAtDesc(
            Long requesterId,
            ApprovalType requestType,
            List<ApprovalStatus> statuses
    );

    int checkPendingApproval(
            Long approvalId,
            Long approverId,
            LocalDateTime confirmedAt
    );
}
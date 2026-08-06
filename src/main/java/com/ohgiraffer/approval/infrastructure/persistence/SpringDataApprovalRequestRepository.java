package com.ohgiraffer.approval.infrastructure.persistence;

import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataApprovalRequestRepository
        extends JpaRepository<ApprovalRequestJpaEntity, Long> {

    List<ApprovalRequestJpaEntity> findByRequesterIdOrderByRequestedAtDesc(
            Long requesterId
    );

    @Query("""
            SELECT approvalRequest
            FROM ApprovalRequestJpaEntity approvalRequest
            WHERE approvalRequest.status = :pendingStatus
               OR approvalRequest.approverId = :userId
            ORDER BY approvalRequest.requestedAt DESC
            """)
    List<ApprovalRequestJpaEntity> findProcessingApprovals(
            @Param("userId") Long userId,
            @Param("pendingStatus") ApprovalStatus pendingStatus
    );
}
package com.ohgiraffer.approval.infrastructure.persistence;

import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;
import com.ohgiraffer.approval.domain.model.approval.ApprovalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataApprovalRequestRepository
        extends JpaRepository<ApprovalRequestJpaEntity, Long> {

    List<ApprovalRequestJpaEntity> findByRequesterIdOrderByRequestedAtDesc(
            Long requesterId
    );

    List<ApprovalRequestJpaEntity> findByRequesterIdAndRequestTypeAndStatusInOrderByRequestedAtDesc(
            Long requesterId,
            ApprovalType requestType,
            List<ApprovalStatus> statuses
    );

    @Query("""
            SELECT approvalRequest
            FROM ApprovalRequestJpaEntity approvalRequest
            WHERE approvalRequest.approverId = :userId
               OR (
                    approvalRequest.status = :pendingStatus
                    AND approvalRequest.requesterId IN (
                        SELECT user.id
                        FROM UserJpaEntity user
                        WHERE user.bootcampId = :bootcampId
                    )
               )
            ORDER BY approvalRequest.requestedAt DESC
            """)
    List<ApprovalRequestJpaEntity> findProcessingApprovals(
            @Param("userId") Long userId,
            @Param("bootcampId") Long bootcampId,
            @Param("pendingStatus") ApprovalStatus pendingStatus
    );

    @Query("""
            SELECT approvalRequest
            FROM ApprovalRequestJpaEntity approvalRequest
            WHERE approvalRequest.requesterId IN (
                SELECT user.id
                FROM UserJpaEntity user
                WHERE user.bootcampId = :bootcampId
            )
            ORDER BY approvalRequest.requestedAt DESC
            """)
    List<ApprovalRequestJpaEntity> findManagerProcessingApprovals(
            @Param("bootcampId") Long bootcampId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ApprovalRequestJpaEntity approvalRequest
            SET approvalRequest.status = :checkedStatus,
                approvalRequest.approverId = :approverId,
                approvalRequest.confirmedAt = :confirmedAt
            WHERE approvalRequest.id = :approvalId
              AND approvalRequest.status = :pendingStatus
            """)
    int checkPendingApproval(
            @Param("approvalId") Long approvalId,
            @Param("approverId") Long approverId,
            @Param("confirmedAt") LocalDateTime confirmedAt,
            @Param("pendingStatus") ApprovalStatus pendingStatus,
            @Param("checkedStatus") ApprovalStatus checkedStatus
    );
}
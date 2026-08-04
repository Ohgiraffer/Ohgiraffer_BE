package com.ohgiraffer.approval.infrastructure.persistence;

import com.ohgiraffer.approval.domain.model.approval.ApprovalRequest;
import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;
import com.ohgiraffer.approval.domain.model.approval.ApprovalType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;

@Entity
@Table(name = "approval_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalRequestJpaEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "approval_id")
    private Long id;

    @Column(
            name = "requester_id",
            nullable = false
    )
    private Long requesterId;

    @Column(name = "approver_id")
    private Long approverId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "request_type",
            nullable = false,
            length = 20
    )
    private ApprovalType requestType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private ApprovalStatus status;

    @Column(
            name = "title",
            nullable = false,
            length = 100
    )
    private String title;

    @Column(name = "reason")
    private String reason;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(
            name = "requested_at",
            nullable = false
    )
    private LocalDateTime requestedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "signature_id")
    private Long signatureId;

    @Lob
    @Column(
            name = "signature_image_snapshot",
            columnDefinition = "LONGBLOB"
    )
    private byte[] signatureImageSnapshot;

    @Column(
            name = "signature_file_type_snapshot",
            length = 50
    )
    private String signatureFileTypeSnapshot;

    private ApprovalRequestJpaEntity(
            Long id,
            Long requesterId,
            Long approverId,
            ApprovalType requestType,
            ApprovalStatus status,
            String title,
            String reason,
            String rejectionReason,
            LocalDateTime requestedAt,
            LocalDateTime confirmedAt,
            LocalDateTime processedAt,
            Long signatureId,
            byte[] signatureImageSnapshot,
            String signatureFileTypeSnapshot
    ) {
        this.id = id;
        this.requesterId = requesterId;
        this.approverId = approverId;
        this.requestType = requestType;
        this.status = status;
        this.title = title;
        this.reason = reason;
        this.rejectionReason = rejectionReason;
        this.requestedAt = requestedAt;
        this.confirmedAt = confirmedAt;
        this.processedAt = processedAt;
        this.signatureId = signatureId;
        this.signatureImageSnapshot = copyBytes(
                signatureImageSnapshot
        );
        this.signatureFileTypeSnapshot = signatureFileTypeSnapshot;
    }

    public static ApprovalRequestJpaEntity from(
            ApprovalRequest approvalRequest
    ) {
        return new ApprovalRequestJpaEntity(
                approvalRequest.getId(),
                approvalRequest.getRequesterId(),
                approvalRequest.getApproverId(),
                approvalRequest.getRequestType(),
                approvalRequest.getStatus(),
                approvalRequest.getTitle(),
                approvalRequest.getReason(),
                approvalRequest.getRejectionReason(),
                approvalRequest.getRequestedAt(),
                approvalRequest.getConfirmedAt(),
                approvalRequest.getProcessedAt(),
                approvalRequest.getSignatureId(),
                approvalRequest.getSignatureImageSnapshot(),
                approvalRequest.getSignatureFileTypeSnapshot()
        );
    }

    public ApprovalRequest toDomain() {
        return ApprovalRequest.restore(
                id,
                requesterId,
                approverId,
                requestType,
                status,
                title,
                reason,
                rejectionReason,
                requestedAt,
                confirmedAt,
                processedAt,
                signatureId,
                signatureImageSnapshot,
                signatureFileTypeSnapshot
        );
    }

    private static byte[] copyBytes(
            byte[] source
    ) {
        if (source == null) {
            return null;
        }

        return Arrays.copyOf(
                source,
                source.length
        );
    }
}
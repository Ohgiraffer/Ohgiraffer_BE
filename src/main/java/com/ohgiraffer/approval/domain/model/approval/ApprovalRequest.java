package com.ohgiraffer.approval.domain.model.approval;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Arrays;

@Getter
public class ApprovalRequest {

    private final Long id;
    private final Long requesterId;
    private Long approverId;
    private final ApprovalType requestType;
    private ApprovalStatus status;
    private final String title;
    private final String reason;
    private String rejectionReason;
    private final LocalDateTime requestedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime processedAt;
    private final Long signatureId;
    private final byte[] signatureImageSnapshot;
    private final String signatureFileTypeSnapshot;

    private ApprovalRequest(
            Long id,
            Long requesterId,
            Long approverId,
            ApprovalType requestType,
            String title,
            String reason,
            LocalDateTime requestedAt,
            Long signatureId,
            byte[] signatureImageSnapshot,
            String signatureFileTypeSnapshot
    ) {
        this.id = id;
        this.requesterId = requesterId;
        this.approverId = approverId;
        this.requestType = requestType;
        this.title = title;
        this.reason = reason;
        this.requestedAt = requestedAt;
        this.signatureId = signatureId;
        this.signatureImageSnapshot = copyBytes(
                signatureImageSnapshot
        );
        this.signatureFileTypeSnapshot = signatureFileTypeSnapshot;
    }

    public static ApprovalRequest createLeave(
            Long requesterId,
            Long signatureId,
            byte[] signatureImageSnapshot,
            String signatureFileTypeSnapshot,
            LocalDateTime requestedAt
    ) {
        ApprovalRequest approvalRequest =
                new ApprovalRequest(
                        null,
                        requesterId,
                        null,
                        ApprovalType.LEAVE,
                        "휴가 신청",
                        null,
                        requestedAt,
                        signatureId,
                        signatureImageSnapshot,
                        signatureFileTypeSnapshot
                );

        approvalRequest.status = ApprovalStatus.PENDING;

        return approvalRequest;
    }

    public static ApprovalRequest createPurchase(
            Long requesterId,
            String reason,
            Long signatureId,
            byte[] signatureImageSnapshot,
            String signatureFileTypeSnapshot,
            LocalDateTime requestedAt
    ) {
        ApprovalRequest approvalRequest =
                new ApprovalRequest(
                        null,
                        requesterId,
                        null,
                        ApprovalType.PURCHASE,
                        "구매 요청",
                        reason,
                        requestedAt,
                        signatureId,
                        signatureImageSnapshot,
                        signatureFileTypeSnapshot
                );

        approvalRequest.status = ApprovalStatus.PENDING;

        return approvalRequest;
    }

    public static ApprovalRequest restore(
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
        ApprovalRequest approvalRequest =
                new ApprovalRequest(
                        id,
                        requesterId,
                        approverId,
                        requestType,
                        title,
                        reason,
                        requestedAt,
                        signatureId,
                        signatureImageSnapshot,
                        signatureFileTypeSnapshot
                );

        approvalRequest.status = status;
        approvalRequest.rejectionReason = rejectionReason;
        approvalRequest.confirmedAt = confirmedAt;
        approvalRequest.processedAt = processedAt;

        return approvalRequest;
    }

    public void check(
            Long approverId,
            LocalDateTime confirmedAt
    ) {
        if (this.status != ApprovalStatus.PENDING) {
            throw new BusinessException(
                    ErrorCode.APPROVAL_INVALID_STATUS
            );
        }

        if (approverId == null || confirmedAt == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        this.approverId = approverId;
        this.status = ApprovalStatus.CHECKED;
        this.confirmedAt = confirmedAt;
    }

    public void approve(
            LocalDateTime processedAt
    ) {
        if (this.status != ApprovalStatus.CHECKED) {
            throw new BusinessException(
                    ErrorCode.APPROVAL_INVALID_STATUS
            );
        }

        if (processedAt == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        this.status = ApprovalStatus.APPROVED;
        this.processedAt = processedAt;
    }

    public void reject(
            String rejectionReason,
            LocalDateTime processedAt
    ) {
        if (this.status != ApprovalStatus.CHECKED) {
            throw new BusinessException(
                    ErrorCode.APPROVAL_INVALID_STATUS
            );
        }

        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (processedAt == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        this.status = ApprovalStatus.REJECTED;
        this.rejectionReason = rejectionReason.strip();
        this.processedAt = processedAt;
    }

    public byte[] getSignatureImageSnapshot() {
        return copyBytes(
                signatureImageSnapshot
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
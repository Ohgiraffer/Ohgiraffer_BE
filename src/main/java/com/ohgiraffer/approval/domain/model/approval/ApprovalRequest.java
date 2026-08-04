package com.ohgiraffer.approval.domain.model.approval;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApprovalRequest {

    private final Long id;
    private final Long requesterId;
    private final Long approverId;
    private final ApprovalType requestType;
    private ApprovalStatus status;
    private final String title;
    private final String reason;
    private String rejectionReason;
    private final LocalDateTime requestedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime processedAt;
    private final Long signatureId;

    public static ApprovalRequest createLeave(
            Long requesterId,
            Long approverId,
            Long signatureId,
            LocalDateTime requestedAt
    ) {
        ApprovalRequest approvalRequest =
                new ApprovalRequest(
                        null,
                        requesterId,
                        approverId,
                        ApprovalType.LEAVE,
                        "휴가 신청",
                        null,
                        requestedAt,
                        signatureId
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
            Long signatureId
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
                        signatureId
                );

        approvalRequest.status = status;
        approvalRequest.rejectionReason = rejectionReason;
        approvalRequest.confirmedAt = confirmedAt;
        approvalRequest.processedAt = processedAt;

        return approvalRequest;
    }

    public void check(
            LocalDateTime checkedAt
    ) {
        validateStatus(
                ApprovalStatus.PENDING,
                "대기 상태의 결재만 확인할 수 있습니다."
        );

        this.status = ApprovalStatus.CHECKED;
        this.confirmedAt = checkedAt;
    }

    public void approve(
            LocalDateTime processedAt
    ) {
        validateStatus(
                ApprovalStatus.CHECKED,
                "확인된 결재만 승인할 수 있습니다."
        );

        this.status = ApprovalStatus.APPROVED;
        this.processedAt = processedAt;
    }

    public void reject(
            String rejectionReason,
            LocalDateTime processedAt
    ) {
        validateStatus(
                ApprovalStatus.CHECKED,
                "확인된 결재만 반려할 수 있습니다."
        );

        this.status = ApprovalStatus.REJECTED;
        this.rejectionReason = rejectionReason;
        this.processedAt = processedAt;
    }

    public void complete(
            LocalDateTime processedAt
    ) {
        validateStatus(
                ApprovalStatus.APPROVED,
                "승인된 결재만 완료 처리할 수 있습니다."
        );

        this.status = ApprovalStatus.COMPLETED;
        this.processedAt = processedAt;
    }

    private void validateStatus(
            ApprovalStatus expectedStatus,
            String message
    ) {
        if (this.status != expectedStatus) {
            throw new IllegalStateException(
                    message
            );
        }
    }
}
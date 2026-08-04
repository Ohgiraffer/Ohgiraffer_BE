package com.ohgiraffer.approval.domain.model.approval;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;

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
    private final byte[] signatureImageSnapshot;
    private final String signatureFileTypeSnapshot;

    public static ApprovalRequest createLeave(
            Long requesterId,
            Long approverId,
            Long signatureId,
            byte[] signatureImageSnapshot,
            String signatureFileTypeSnapshot,
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
                        signatureId,
                        copyBytes(signatureImageSnapshot),
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
                        copyBytes(signatureImageSnapshot),
                        signatureFileTypeSnapshot
                );

        approvalRequest.status = status;
        approvalRequest.rejectionReason = rejectionReason;
        approvalRequest.confirmedAt = confirmedAt;
        approvalRequest.processedAt = processedAt;

        return approvalRequest;
    }

    public byte[] getSignatureImageSnapshot() {
        return copyBytes(signatureImageSnapshot);
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
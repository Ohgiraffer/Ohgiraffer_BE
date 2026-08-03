package com.ohgiraffer.approval.presentation.api.request;

import java.time.LocalDate;

public record CreateLeaveApprovalRequest(
        Long approverId,
        LocalDate startDate,
        LocalDate endDate,
        Long signatureId
) {
}
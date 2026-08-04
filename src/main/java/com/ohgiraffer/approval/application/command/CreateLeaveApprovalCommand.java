package com.ohgiraffer.approval.application.command;

import java.time.LocalDate;

public record CreateLeaveApprovalCommand(
        Long requesterId,
        Long approverId,
        LocalDate startDate,
        LocalDate endDate
) {
}
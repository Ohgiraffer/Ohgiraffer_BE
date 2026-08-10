package com.ohgiraffer.approval.application.port;

import java.time.LocalDate;

public interface ApplyApprovedLeavePort {
    void applyApprovedLeave(Long userId, LocalDate startDate, LocalDate endDate, Long approvalId);
}

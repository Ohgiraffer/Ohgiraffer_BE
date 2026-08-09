package com.ohgiraffer.attendance.application.usecase;

import java.time.LocalDate;

public interface AttendanceCommandUsecase {
    void applyApprovedLeave(Long userId, LocalDate startDate, LocalDate endDate, Long approvalId);

}

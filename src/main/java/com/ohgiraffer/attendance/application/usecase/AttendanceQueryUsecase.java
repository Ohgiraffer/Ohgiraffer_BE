package com.ohgiraffer.attendance.application.usecase;

import com.ohgiraffer.attendance.presentation.api.response.AttendanceSummaryResponse;
import com.ohgiraffer.attendance.presentation.api.response.MonthlyAttendanceResponse;

import java.time.YearMonth;

public interface AttendanceQueryUsecase {
    MonthlyAttendanceResponse getMonthlyAttendance(Long userId, YearMonth yearMonth);
    AttendanceSummaryResponse getSummary(Long userId);

    MonthlyAttendanceResponse getMonthlyAttendanceForManager(Long requesterId, Long targetUserId, YearMonth yearMonth);
    AttendanceSummaryResponse getSummaryForManager(Long requesterId, Long targetUserId);
}

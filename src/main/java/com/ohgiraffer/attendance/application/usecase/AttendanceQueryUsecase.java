package com.ohgiraffer.attendance.application.usecase;

import com.ohgiraffer.attendance.presentation.api.response.MonthlyAttendanceResponse;

import java.time.YearMonth;

public interface AttendanceQueryUsecase {
    MonthlyAttendanceResponse getMonthlyAttendance(Long userId, YearMonth yearMonth);
}

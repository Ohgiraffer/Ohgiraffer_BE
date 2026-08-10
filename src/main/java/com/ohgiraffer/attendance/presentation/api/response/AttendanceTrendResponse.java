package com.ohgiraffer.attendance.presentation.api.response;

import com.ohgiraffer.attendance.domain.model.DailyAttendanceCountView;

import java.time.LocalDate;

public record AttendanceTrendResponse(
        LocalDate date,
        long presentCount,
        long absentCount
) {
    public static AttendanceTrendResponse from(DailyAttendanceCountView view) {
        return new AttendanceTrendResponse(view.date(), view.presentCount(), view.absentCount());
    }
}
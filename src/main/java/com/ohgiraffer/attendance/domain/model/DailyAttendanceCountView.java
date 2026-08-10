package com.ohgiraffer.attendance.domain.model;

import java.time.LocalDate;

public record DailyAttendanceCountView(
        LocalDate date,
        long presentCount,
        long absentCount
) {
}

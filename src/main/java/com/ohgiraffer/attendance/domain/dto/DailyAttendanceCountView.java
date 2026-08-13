package com.ohgiraffer.attendance.domain.dto;

import java.time.LocalDate;

public record DailyAttendanceCountView(
        LocalDate date,
        long presentCount,
        long absentCount
) {
}

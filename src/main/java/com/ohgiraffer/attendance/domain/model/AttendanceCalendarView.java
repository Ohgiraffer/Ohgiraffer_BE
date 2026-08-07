package com.ohgiraffer.attendance.domain.model;

import java.time.LocalDate;

public record AttendanceCalendarView(
        LocalDate attendanceDate,
        AttendanceStatus status
) {
}

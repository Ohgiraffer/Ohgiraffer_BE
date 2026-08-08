package com.ohgiraffer.attendance.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceCalendarView(
        LocalDate attendanceDate,
        AttendanceStatus status,
        LocalTime checkInTime,
        LocalTime checkOutTime
) {
}
package com.ohgiraffer.attendance.infrastructure.projection;

import com.ohgiraffer.attendance.domain.model.AttendanceStatus;

import java.time.LocalDate;

public interface AttendanceCalendarProjection {
    LocalDate getAttendanceDate();
    AttendanceStatus getStatus();
}
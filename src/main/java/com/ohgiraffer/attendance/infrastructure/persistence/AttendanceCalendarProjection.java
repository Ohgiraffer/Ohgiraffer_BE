package com.ohgiraffer.attendance.infrastructure.persistence;

import com.ohgiraffer.attendance.domain.model.AttendanceStatus;

import java.time.LocalDate;

public interface AttendanceCalendarProjection {
    LocalDate getAttendanceDate();
    AttendanceStatus getStatus();
}
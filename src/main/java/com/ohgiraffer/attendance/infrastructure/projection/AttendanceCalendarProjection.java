package com.ohgiraffer.attendance.infrastructure.projection;

import com.ohgiraffer.attendance.domain.model.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public interface AttendanceCalendarProjection {
    LocalDate getAttendanceDate();
    AttendanceStatus getStatus();
    LocalTime getCheckInTime();
    LocalTime getCheckOutTime();
}
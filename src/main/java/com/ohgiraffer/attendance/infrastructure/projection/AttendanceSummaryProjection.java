package com.ohgiraffer.attendance.infrastructure.projection;

public interface AttendanceSummaryProjection {
    Long getPresentDays();
    Long getLateCount();
    Long getEarlyLeaveCount();
    Long getOutingCount();
    Long getAbsentDays();
    Long getLeaveDays();
    Long getSickDays();
}

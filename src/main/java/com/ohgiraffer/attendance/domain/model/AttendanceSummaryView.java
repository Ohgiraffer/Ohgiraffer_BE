package com.ohgiraffer.attendance.domain.model;

public record AttendanceSummaryView(
        long presentDays,
        long lateCount,
        long earlyLeaveCount,
        long outingCount,
        long absentDays,
        long leaveDays,
        long sickDays
) {
}

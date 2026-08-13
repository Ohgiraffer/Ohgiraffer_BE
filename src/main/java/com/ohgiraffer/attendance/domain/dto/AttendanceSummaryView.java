package com.ohgiraffer.attendance.domain.dto;

public record AttendanceSummaryView(
        long presentDays,
        long lateCount,
        long earlyLeaveCount,
        long outingCount,
        long absentDays,
        long leaveDays,
        long sickDays
) {
    public static AttendanceSummaryView empty() {
        return new AttendanceSummaryView(0, 0, 0, 0, 0, 0, 0);
    }
}

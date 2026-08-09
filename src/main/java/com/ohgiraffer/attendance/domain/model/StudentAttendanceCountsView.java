package com.ohgiraffer.attendance.domain.model;

public record StudentAttendanceCountsView(
        Long userId,
        long presentDays,
        long lateCount,
        long earlyLeaveCount,
        long outingCount,
        long absentDays,
        long leaveDays,
        long sickDays
) {
    public static StudentAttendanceCountsView empty(Long userId) {
        return new StudentAttendanceCountsView(userId, 0, 0, 0, 0, 0, 0, 0);
    }
}
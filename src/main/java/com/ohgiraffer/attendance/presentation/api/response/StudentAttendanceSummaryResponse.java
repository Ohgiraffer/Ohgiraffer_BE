package com.ohgiraffer.attendance.presentation.api.response;

import com.ohgiraffer.attendance.domain.model.AttendanceRiskLevel;
import com.ohgiraffer.attendance.domain.model.StudentAttendanceCountsView;

import java.math.BigDecimal;

public record StudentAttendanceSummaryResponse(
        String name,
        BigDecimal attendanceRate,
        long lateCount,
        long earlyLeaveCount,
        long outingCount,
        long absentDays,
        AttendanceRiskLevel status
) {
    public static StudentAttendanceSummaryResponse of(
            String name, BigDecimal rate, StudentAttendanceCountsView counts, AttendanceRiskLevel riskLevel
    ) {
        return new StudentAttendanceSummaryResponse(
                name, rate,
                counts.lateCount(), counts.earlyLeaveCount(), counts.outingCount(), counts.absentDays(),
                riskLevel
        );
    }
}
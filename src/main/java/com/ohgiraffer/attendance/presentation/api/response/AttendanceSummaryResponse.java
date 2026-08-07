package com.ohgiraffer.attendance.presentation.api.response;

import com.ohgiraffer.attendance.domain.model.AttendanceRiskLevel;
import com.ohgiraffer.attendance.domain.model.AttendanceSummaryView;

import java.math.BigDecimal;

public record AttendanceSummaryResponse(
        long presentDays,
        long lateCount,
        long earlyLeaveCount,
        long outingCount,
        long absentDays,
        long leaveDays,
        long sickDays,
        BigDecimal attendanceRate,
        AttendanceRiskLevel riskLevel  // null이면 정상
) {
    public static AttendanceSummaryResponse of(
            AttendanceSummaryView view, BigDecimal attendanceRate, AttendanceRiskLevel riskLevel
    ) {
        return new AttendanceSummaryResponse(
                view.presentDays(),
                view.lateCount(),
                view.earlyLeaveCount(),
                view.outingCount(),
                view.absentDays(),
                view.leaveDays(),
                view.sickDays(),
                attendanceRate,
                riskLevel
        );
    }
}
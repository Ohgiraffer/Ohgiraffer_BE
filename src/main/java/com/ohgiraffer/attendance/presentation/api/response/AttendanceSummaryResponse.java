package com.ohgiraffer.attendance.presentation.api.response;

import com.ohgiraffer.attendance.domain.model.AttendanceRiskLevel;
import com.ohgiraffer.attendance.domain.dto.AttendanceSummaryView;
import com.ohgiraffer.attendance.domain.dto.PeriodAttendanceRate;

import java.math.BigDecimal;
import java.util.List;

public record AttendanceSummaryResponse(
        long presentDays,
        long lateCount,
        long earlyLeaveCount,
        long outingCount,
        long absentDays,
        long leaveDays,
        long sickDays,
        BigDecimal attendanceRate,
        AttendanceRiskLevel riskLevel,
        List<PeriodAttendanceRate> periodRates
) {
    public static AttendanceSummaryResponse of(
            AttendanceSummaryView view, BigDecimal attendanceRate,
            AttendanceRiskLevel riskLevel, List<PeriodAttendanceRate> periodRates
    ) {
        return new AttendanceSummaryResponse(
                view.presentDays(), view.lateCount(), view.earlyLeaveCount(),
                view.outingCount(), view.absentDays(), view.leaveDays(), view.sickDays(),
                attendanceRate, riskLevel, periodRates
        );
    }
}
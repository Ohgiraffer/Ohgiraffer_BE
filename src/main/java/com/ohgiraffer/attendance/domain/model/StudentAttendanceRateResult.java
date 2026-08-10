package com.ohgiraffer.attendance.domain.model;

import java.math.BigDecimal;

public record StudentAttendanceRateResult(
        BigDecimal attendanceRate,
        AttendanceRiskLevel riskLevel,
        StudentAttendanceCountsView counts
) {
}
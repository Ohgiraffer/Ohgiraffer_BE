package com.ohgiraffer.attendance.domain.dto;

import com.ohgiraffer.attendance.domain.model.AttendanceRiskLevel;

import java.math.BigDecimal;

public record StudentAttendanceRateResult(
        BigDecimal attendanceRate,
        AttendanceRiskLevel riskLevel,
        StudentAttendanceCountsView counts
) {
}
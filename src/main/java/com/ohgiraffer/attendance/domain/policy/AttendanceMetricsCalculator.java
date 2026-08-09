package com.ohgiraffer.attendance.domain.policy;

import com.ohgiraffer.attendance.domain.model.AttendanceRiskLevel;
import com.ohgiraffer.bootcamp.domain.model.AttendancePolicyResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class AttendanceMetricsCalculator {

    private AttendanceMetricsCalculator() {
    }

    public static BigDecimal calculateAttendanceRate(
            LocalDate start,
            LocalDate end,
            long absentDays,
            long lateCount,
            long earlyLeaveCount,
            long outingCount,
            int conversionCount
    ) {
        long totalDays = ChronoUnit.DAYS.between(start, end) + 1;

        long irregularCount = lateCount + earlyLeaveCount + outingCount;
        long convertedAbsences = conversionCount > 0 ? irregularCount / conversionCount : 0;

        long effectiveAbsentDays = absentDays + convertedAbsences;
        long attendedDays = Math.max(totalDays - effectiveAbsentDays, 0);

        return BigDecimal.valueOf(attendedDays)
                .divide(BigDecimal.valueOf(totalDays), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static AttendanceRiskLevel calculateRiskLevel(BigDecimal attendanceRate, AttendancePolicyResult policy) {
        if (attendanceRate.compareTo(policy.periodExpulsionPct()) <= 0) {
            return AttendanceRiskLevel.RISK;
        }
        if (policy.warningThresholdPct() != null && attendanceRate.compareTo(policy.warningThresholdPct()) <= 0) {
            return AttendanceRiskLevel.WARNING;
        }
        if (policy.cautionThresholdPct() != null && attendanceRate.compareTo(policy.cautionThresholdPct()) <= 0) {
            return AttendanceRiskLevel.CAUTION;
        }
        return null; // 정상
    }
}
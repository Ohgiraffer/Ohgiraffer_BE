package com.ohgiraffer.attendance.domain.policy;

import com.ohgiraffer.attendance.domain.model.AttendanceRiskLevel;
import com.ohgiraffer.bootcamp.domain.model.AttendancePolicyResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
        long totalDays = countWeekdays(start, end);
        if (totalDays <= 0) {
            return BigDecimal.ZERO;
        }

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
        return null;
    }

    public static long countWeekdays(LocalDate start, LocalDate end) {
        long totalDays = ChronoUnit.DAYS.between(start, end) + 1;
        long fullWeeks = totalDays / 7;
        long weekdayCount = fullWeeks * 5;

        long remainingDays = totalDays % 7;
        LocalDate cursor = end.minusDays(remainingDays - 1);
        for (int i = 0; i < remainingDays; i++) {
            DayOfWeek dow = cursor.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                weekdayCount++;
            }
            cursor = cursor.plusDays(1);
        }
        return weekdayCount;
    }

    public static List<LocalDate> weekdaysBetween(LocalDate start, LocalDate end) {
        return start.datesUntil(end.plusDays(1))
                .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY)
                .toList();
    }
}
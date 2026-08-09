package com.ohgiraffer.attendance.application.cache;

import com.ohgiraffer.attendance.domain.model.AttendanceRiskLevel;
import com.ohgiraffer.attendance.domain.model.AttendanceSummaryView;
import com.ohgiraffer.attendance.domain.model.PeriodAttendanceRate;
import com.ohgiraffer.attendance.domain.repository.AttendanceRepository;
import com.ohgiraffer.attendance.presentation.api.response.AttendanceSummaryResponse;
import com.ohgiraffer.bootcamp.application.usecase.BootcampQueryUsecase;
import com.ohgiraffer.bootcamp.domain.model.AttendancePeriodResult;
import com.ohgiraffer.bootcamp.domain.model.AttendancePolicyResult;
import com.ohgiraffer.bootcamp.domain.model.BootcampPeriodResult;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Component
public class AttendanceSummaryCache {

    private final AttendanceRepository attendanceRepository;
    private final UserQueryUsecase userQueryUsecase;
    private final BootcampQueryUsecase bootcampQueryUsecase;

    private static final int LATE_EARLY_LEAVE_CONVERSION_COUNT = 3;

    @Cacheable(value = "attendanceSummary", key = "#userId + '-' + T(java.time.LocalDate).now()")
    public AttendanceSummaryResponse getCachedSummary(Long userId) {
        Long bootcampId = userQueryUsecase.getBootcampId(userId);
        BootcampPeriodResult bootcampPeriod = bootcampQueryUsecase.getPeriod(bootcampId);
        AttendancePolicyResult policy = bootcampQueryUsecase.getPolicy(bootcampId);

        LocalDate today = LocalDate.now();

        if (today.isBefore(bootcampPeriod.startDate())) {
            return AttendanceSummaryResponse.of(AttendanceSummaryView.empty(), null, null, List.of());
        }

        LocalDate start = bootcampPeriod.startDate();
        LocalDate end = today.isBefore(bootcampPeriod.endDate()) ? today : bootcampPeriod.endDate();

        AttendanceSummaryView summary = attendanceRepository.countByUserAndDateRange(userId, start, end);

        long totalDays = countWeekdays(start, end);
        BigDecimal attendanceRate = calculateAttendanceRate(summary, totalDays);
        AttendanceRiskLevel riskLevel = calculateRiskLevel(attendanceRate, policy);

        List<PeriodAttendanceRate> periodRates = calculatePeriodRates(userId, bootcampId, today);

        return AttendanceSummaryResponse.of(summary, attendanceRate, riskLevel, periodRates);
    }

    private List<PeriodAttendanceRate> calculatePeriodRates(Long userId, Long bootcampId, LocalDate today) {
        List<AttendancePeriodResult> periods = bootcampQueryUsecase.getAttendancePeriods(bootcampId);

        return periods.stream()
                .filter(period -> !today.isBefore(period.startDate()))
                .map(period -> {
                    LocalDate periodEnd = today.isBefore(period.endDate()) ? today : period.endDate();

                    AttendanceSummaryView periodSummary =
                            attendanceRepository.countByUserAndDateRange(userId, period.startDate(), periodEnd);

                    long periodTotalDays = countWeekdays(period.startDate(), periodEnd);
                    BigDecimal periodRate = calculateAttendanceRate(periodSummary, periodTotalDays);

                    return new PeriodAttendanceRate(period.periodNo(), periodRate);
                })
                .toList();
    }

    private BigDecimal calculateAttendanceRate(AttendanceSummaryView summary, long totalDays) {
        if (totalDays <= 0) {
            return BigDecimal.ZERO;
        }

        long irregularCount = summary.lateCount() + summary.earlyLeaveCount() + summary.outingCount();
        long convertedAbsences = irregularCount / LATE_EARLY_LEAVE_CONVERSION_COUNT;
        long effectiveAbsentDays = summary.absentDays() + convertedAbsences;

        long attendedDays = Math.max(totalDays - effectiveAbsentDays, 0);

        return BigDecimal.valueOf(attendedDays)
                .divide(BigDecimal.valueOf(totalDays), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private AttendanceRiskLevel calculateRiskLevel(BigDecimal attendanceRate, AttendancePolicyResult policy) {
        if (attendanceRate.compareTo(policy.periodExpulsionPct()) <= 0) {
            return AttendanceRiskLevel.RISK;
        }
        if (attendanceRate.compareTo(policy.warningThresholdPct()) <= 0) {
            return AttendanceRiskLevel.WARNING;
        }
        if (attendanceRate.compareTo(policy.cautionThresholdPct()) <= 0) {
            return AttendanceRiskLevel.CAUTION;
        }
        return null;
    }

    private long countWeekdays(LocalDate start, LocalDate end) {
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
}
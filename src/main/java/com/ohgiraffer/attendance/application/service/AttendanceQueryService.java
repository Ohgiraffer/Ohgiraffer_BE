package com.ohgiraffer.attendance.application.service;

import com.ohgiraffer.attendance.application.usecase.AttendanceQueryUsecase;
import com.ohgiraffer.attendance.domain.model.AttendanceCalendarView;
import com.ohgiraffer.attendance.domain.model.AttendanceRiskLevel;
import com.ohgiraffer.attendance.domain.model.AttendanceSummaryView;
import com.ohgiraffer.attendance.domain.model.CalendarStatusGroup;
import com.ohgiraffer.attendance.domain.repository.AttendanceRepository;
import com.ohgiraffer.attendance.presentation.api.response.AttendanceSummaryResponse;
import com.ohgiraffer.attendance.presentation.api.response.MonthlyAttendanceResponse;
import com.ohgiraffer.bootcamp.application.usecase.BootcampQueryUsecase;
import com.ohgiraffer.bootcamp.domain.model.AttendancePolicyResult;
import com.ohgiraffer.bootcamp.domain.model.BootcampPeriodResult;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@Service
public class AttendanceQueryService implements AttendanceQueryUsecase {

    private final AttendanceRepository attendanceRepository;
    private final UserQueryUsecase userQueryUsecase;
    private final BootcampQueryUsecase bootcampQueryUsecase;

    private static final int LATE_EARLY_LEAVE_CONVERSION_COUNT = 3;

    @Override
    public MonthlyAttendanceResponse getMonthlyAttendance(Long userId, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<AttendanceCalendarView> views =
                attendanceRepository.findCalendarByUserIdAndDateRange(userId, start, end);

        Map<LocalDate, CalendarStatusGroup> statusByDate = views.stream()
                .collect(Collectors.toMap(
                        AttendanceCalendarView::attendanceDate,
                        v -> CalendarStatusGroup.from(v.status()),
                        (existing, duplicate) -> {
                            log.warn("[getMonthlyAttendance] 동일 날짜 출결 중복 발견, 기존 값 유지 | userId={}, date={}",
                                    userId, existing);
                            return existing;
                        }
                ));

        List<MonthlyAttendanceResponse.DayInfo> days = Stream.iterate(start, d -> d.plusDays(1))
                .limit(end.getDayOfMonth())
                .map(date -> new MonthlyAttendanceResponse.DayInfo(
                        date,
                        statusByDate.get(date)
                ))
                .toList();

        return new MonthlyAttendanceResponse(yearMonth.toString(), days);
    }

    @Override
    @Cacheable(value = "attendanceSummary", key = "#userId + '-' + T(java.time.LocalDate).now()")
    public AttendanceSummaryResponse getSummary(Long userId) {
        Long bootcampId = userQueryUsecase.getBootcampId(userId);
        BootcampPeriodResult bootcampPeriod = bootcampQueryUsecase.getPeriod(bootcampId);
        AttendancePolicyResult policy = bootcampQueryUsecase.getPolicy(bootcampId);

        LocalDate today = LocalDate.now();

        if (today.isBefore(bootcampPeriod.startDate())) {
            return AttendanceSummaryResponse.of(AttendanceSummaryView.empty(), null, null);
        }

        LocalDate start = bootcampPeriod.startDate();
        LocalDate end = today.isBefore(bootcampPeriod.endDate()) ? today : bootcampPeriod.endDate();

        AttendanceSummaryView summary = attendanceRepository.countByUserAndDateRange(userId, start, end);

        long totalDays = countWeekdays(start, end);
        BigDecimal attendanceRate = calculateAttendanceRate(summary, totalDays);
        AttendanceRiskLevel riskLevel = calculateRiskLevel(attendanceRate, policy);

        return AttendanceSummaryResponse.of(summary, attendanceRate, riskLevel);
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
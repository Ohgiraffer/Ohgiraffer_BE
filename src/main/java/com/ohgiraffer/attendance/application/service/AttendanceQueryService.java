package com.ohgiraffer.attendance.application.service;

import com.ohgiraffer.attendance.application.cache.AttendanceSummaryCache;
import com.ohgiraffer.attendance.application.policy.BootcampAccessPolicy;
import com.ohgiraffer.attendance.application.usecase.AttendanceQueryUsecase;
import com.ohgiraffer.attendance.domain.model.*;
import com.ohgiraffer.attendance.domain.policy.AttendanceMetricsCalculator;
import com.ohgiraffer.attendance.domain.repository.AttendancePeriodSummaryRepository;
import com.ohgiraffer.attendance.domain.repository.AttendanceRepository;
import com.ohgiraffer.attendance.domain.repository.LeaveBalanceRepository;
import com.ohgiraffer.attendance.domain.repository.SickBalanceRepository;
import com.ohgiraffer.attendance.presentation.api.response.*;
import com.ohgiraffer.attendance.application.port.GetUserNamesPort;
import com.ohgiraffer.bootcamp.application.usecase.BootcampQueryUsecase;
import com.ohgiraffer.bootcamp.domain.model.AttendancePolicyResult;
import com.ohgiraffer.bootcamp.domain.model.BootcampPeriodResult;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import com.ohgiraffer.user.domain.model.StudentStatusView;
import com.ohgiraffer.user.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@Service
public class AttendanceQueryService implements AttendanceQueryUsecase {


    private final AttendanceRepository attendanceRepository;
    private final BootcampAccessPolicy bootcampAccessPolicy;
    private final AttendanceSummaryCache attendanceSummaryCache;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final SickBalanceRepository sickBalanceRepository;
    private final UserQueryUsecase userQueryUsecase;
    private final BootcampQueryUsecase bootcampQueryUsecase;
    private final GetUserNamesPort getUserNamesPort;
    private final AttendancePeriodSummaryRepository attendancePeriodSummaryRepository;

    private static final int LATE_EARLY_OUTING_CONVERSION_COUNT = 3;

    @Override
    public MonthlyAttendanceResponse getMonthlyAttendance(Long userId, YearMonth yearMonth) {
        return buildMonthlyAttendance(userId, yearMonth);
    }

    @Override
    public MonthlyAttendanceResponse getMonthlyAttendanceForManager(Long requesterId, Long targetUserId, YearMonth yearMonth) {
        bootcampAccessPolicy.validateSameBootcamp(requesterId, targetUserId);
        return buildMonthlyAttendance(targetUserId, yearMonth);
    }

    @Override
    public AttendanceSummaryResponse getSummary(Long userId) {
        return attendanceSummaryCache.getCachedSummary(userId);
    }

    @Override
    public AttendanceSummaryResponse getSummaryForManager(Long requesterId, Long targetUserId) {
        bootcampAccessPolicy.validateSameBootcamp(requesterId, targetUserId);
        return attendanceSummaryCache.getCachedSummary(targetUserId);
    }

    @Override
    public AttendanceBalanceResponse getLeaveBalance(Long userId) {
        return buildBalance(userId);
    }

    @Override
    public AttendanceBalanceResponse getLeaveBalanceForManager(Long requesterId, Long targetUserId) {
        bootcampAccessPolicy.validateSameBootcamp(requesterId, targetUserId);
        return buildBalance(targetUserId);
    }

    @Override
    public List<StudentAttendanceSummaryResponse> getSummaries(Long requesterId) {
        Long bootcampId = userQueryUsecase.getBootcampId(requesterId);

        List<Long> studentIds = userQueryUsecase.getStudentIdsByBootcampId(bootcampId);
        Map<Long, String> nameByUserId = getUserNamesPort.findNamesByUserIds(studentIds);

        Map<Long, StudentAttendanceCountsView> countsByUserId = attendancePeriodSummaryRepository
                .aggregateByUserIds(studentIds).stream()
                .collect(Collectors.toMap(StudentAttendanceCountsView::userId, Function.identity()));

        AttendancePolicyResult policy = bootcampQueryUsecase.getPolicy(bootcampId);
        BootcampPeriodResult bootcampPeriod = bootcampQueryUsecase.getPeriod(bootcampId);
        LocalDate today = LocalDate.now();

        return studentIds.stream()
                .map(userId -> {
                    String name = nameByUserId.getOrDefault(userId, "알 수 없음");
                    StudentAttendanceCountsView counts = countsByUserId
                            .getOrDefault(userId, StudentAttendanceCountsView.empty(userId));

                    BigDecimal rate = AttendanceMetricsCalculator.calculateAttendanceRate(
                            bootcampPeriod.startDate(), today,
                            counts.absentDays(), counts.lateCount(), counts.earlyLeaveCount(), counts.outingCount(),
                            LATE_EARLY_OUTING_CONVERSION_COUNT
                    );
                    AttendanceRiskLevel riskLevel = AttendanceMetricsCalculator.calculateRiskLevel(rate, policy);

                    return StudentAttendanceSummaryResponse.of(name, rate, counts, riskLevel);
                })
                .toList();
    }

    @Override
    public AttendanceDashboardSummaryResponse getDashboardSummary(Long requesterId) {
        Long bootcampId = userQueryUsecase.getBootcampId(requesterId);

        List<StudentStatusView> statuses = userQueryUsecase.getStudentStatusesByBootcampId(bootcampId);

        int totalStudents = statuses.size();

        List<Long> activeIds = statuses.stream()
                .filter(s -> s.status() == UserStatus.ACTIVE)
                .map(StudentStatusView::userId)
                .toList();
        int activeStudents = activeIds.size();

        int dropoutStudents = (int) statuses.stream()
                .filter(s -> s.status() == UserStatus.WITHDRAWN || s.status() == UserStatus.EXPELLED)
                .count();

        Map<Long, StudentAttendanceCountsView> countsByUserId = attendancePeriodSummaryRepository
                .aggregateByUserIds(activeIds).stream()
                .collect(Collectors.toMap(StudentAttendanceCountsView::userId, Function.identity()));

        AttendancePolicyResult policy = bootcampQueryUsecase.getPolicy(bootcampId);
        BootcampPeriodResult bootcampPeriod = bootcampQueryUsecase.getPeriod(bootcampId);
        LocalDate today = LocalDate.now();

        List<BigDecimal> rates = new java.util.ArrayList<>();
        int atRiskStudents = 0;

        for (Long userId : activeIds) {
            StudentAttendanceCountsView counts = countsByUserId
                    .getOrDefault(userId, StudentAttendanceCountsView.empty(userId));

            BigDecimal rate = AttendanceMetricsCalculator.calculateAttendanceRate(
                    bootcampPeriod.startDate(), today,
                    counts.absentDays(), counts.lateCount(), counts.earlyLeaveCount(), counts.outingCount(),
                    LATE_EARLY_OUTING_CONVERSION_COUNT
            );
            rates.add(rate);

            if (AttendanceMetricsCalculator.calculateRiskLevel(rate, policy) != null) {
                atRiskStudents++;
            }
        }

        BigDecimal averageAttendanceRate = rates.isEmpty()
                ? BigDecimal.ZERO
                : rates.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(rates.size()), 2, RoundingMode.HALF_UP);

        int managedStudents = activeStudents - atRiskStudents;

        BigDecimal expectedCompletionRate = totalStudents == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(totalStudents - dropoutStudents - atRiskStudents)
                .divide(BigDecimal.valueOf(totalStudents), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        return new AttendanceDashboardSummaryResponse(
                averageAttendanceRate, expectedCompletionRate,
                totalStudents, activeStudents, managedStudents, atRiskStudents, dropoutStudents
        );
    }

    private AttendanceBalanceResponse buildBalance(Long userId) {
        LocalDate today = LocalDate.now();

        int remainingLeave = leaveBalanceRepository.findCurrentByUserId(userId, today)
                .map(LeaveBalance::remainingDays)
                .orElse(0);

        int remainingSick = sickBalanceRepository.findCurrentByUserId(userId, today)
                .map(SickBalance::remainingDays)
                .orElse(0);

        return AttendanceBalanceResponse.of(remainingLeave, remainingSick);
    }

    private MonthlyAttendanceResponse buildMonthlyAttendance(Long userId, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<AttendanceCalendarView> views =
                attendanceRepository.findCalendarByUserIdAndDateRange(userId, start, end);

        Map<LocalDate, AttendanceCalendarView> viewByDate = views.stream()
                .collect(Collectors.toMap(
                        AttendanceCalendarView::attendanceDate,
                        v -> v,
                        (existing, duplicate) -> {
                            log.warn("[buildMonthlyAttendance] 동일 날짜 출결 중복 발견, 기존 값 유지 | userId={}, date={}",
                                    userId, existing.attendanceDate());
                            return existing;
                        }
                ));

        List<MonthlyAttendanceResponse.DayInfo> days = Stream.iterate(start, d -> d.plusDays(1))
                .limit(end.getDayOfMonth())
                .map(date -> {
                    AttendanceCalendarView view = viewByDate.get(date);
                    return new MonthlyAttendanceResponse.DayInfo(
                            date,
                            view != null ? CalendarStatusGroup.from(view.status()) : null,
                            view != null ? view.checkInTime() : null,
                            view != null ? view.checkOutTime() : null
                    );
                })
                .toList();

        return new MonthlyAttendanceResponse(yearMonth.toString(), days);
    }
}
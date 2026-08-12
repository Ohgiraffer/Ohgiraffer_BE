package com.ohgiraffer.attendance.application.helper;
import com.ohgiraffer.attendance.domain.model.AttendanceRiskLevel;
import com.ohgiraffer.attendance.domain.dto.StudentAttendanceCountsView;
import com.ohgiraffer.attendance.domain.dto.StudentAttendanceRateResult;
import com.ohgiraffer.attendance.domain.policy.AttendanceMetricsCalculator;
import com.ohgiraffer.attendance.domain.repository.AttendancePeriodSummaryRepository;
import com.ohgiraffer.bootcamp.application.usecase.BootcampQueryUsecase;
import com.ohgiraffer.bootcamp.domain.model.AttendancePeriodResult;
import com.ohgiraffer.bootcamp.domain.model.AttendancePolicyResult;
import com.ohgiraffer.bootcamp.domain.model.BootcampPeriodResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StudentAttendanceRateResolver {


    private final AttendancePeriodSummaryRepository attendancePeriodSummaryRepository;
    private final BootcampQueryUsecase bootcampQueryUsecase;

    public Map<Long, StudentAttendanceRateResult> resolve(Long bootcampId, List<Long> userIds) {
        List<Long> periodIds = bootcampQueryUsecase.getAttendancePeriods(bootcampId).stream()
                .map(AttendancePeriodResult::id)
                .toList();

        Map<Long, StudentAttendanceCountsView> countsByUserId = attendancePeriodSummaryRepository
                .aggregateByUserIds(userIds, periodIds).stream()
                .collect(Collectors.toMap(StudentAttendanceCountsView::userId, Function.identity()));

        AttendancePolicyResult policy = bootcampQueryUsecase.getPolicy(bootcampId);
        BootcampPeriodResult bootcampPeriod = bootcampQueryUsecase.getPeriod(bootcampId);

        LocalDate today = LocalDate.now();
        LocalDate end = today.isBefore(bootcampPeriod.endDate()) ? today : bootcampPeriod.endDate();

        Map<Long, StudentAttendanceRateResult> result = new LinkedHashMap<>();
        for (Long userId : userIds) {
            StudentAttendanceCountsView counts = countsByUserId
                    .getOrDefault(userId, StudentAttendanceCountsView.empty(userId));

            BigDecimal rate = AttendanceMetricsCalculator.calculateAttendanceRate(
                    bootcampPeriod.startDate(), end,
                    counts.absentDays(), counts.lateCount(), counts.earlyLeaveCount(), counts.outingCount()
            );
            AttendanceRiskLevel riskLevel = AttendanceMetricsCalculator.calculateRiskLevel(rate, policy);

            result.put(userId, new StudentAttendanceRateResult(rate, riskLevel, counts));
        }
        return result;
    }
}
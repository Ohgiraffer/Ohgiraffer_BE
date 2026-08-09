package com.ohgiraffer.attendance.application.helper;
import com.ohgiraffer.attendance.domain.model.AttendanceRiskLevel;
import com.ohgiraffer.attendance.domain.model.StudentAttendanceCountsView;
import com.ohgiraffer.attendance.domain.model.StudentAttendanceRateResult;
import com.ohgiraffer.attendance.domain.policy.AttendanceMetricsCalculator;
import com.ohgiraffer.attendance.domain.repository.AttendancePeriodSummaryRepository;
import com.ohgiraffer.bootcamp.application.usecase.BootcampQueryUsecase;
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


    private static final int LATE_EARLY_OUTING_CONVERSION_COUNT = 3;

    private final AttendancePeriodSummaryRepository attendancePeriodSummaryRepository;
    private final BootcampQueryUsecase bootcampQueryUsecase;

    public Map<Long, StudentAttendanceRateResult> resolve(Long bootcampId, List<Long> userIds) {
        Map<Long, StudentAttendanceCountsView> countsByUserId = attendancePeriodSummaryRepository
                .aggregateByUserIds(userIds).stream()
                .collect(Collectors.toMap(StudentAttendanceCountsView::userId, Function.identity()));

        AttendancePolicyResult policy = bootcampQueryUsecase.getPolicy(bootcampId);
        BootcampPeriodResult bootcampPeriod = bootcampQueryUsecase.getPeriod(bootcampId);
        LocalDate today = LocalDate.now();

        Map<Long, StudentAttendanceRateResult> result = new LinkedHashMap<>();
        for (Long userId : userIds) {
            StudentAttendanceCountsView counts = countsByUserId
                    .getOrDefault(userId, StudentAttendanceCountsView.empty(userId));

            BigDecimal rate = AttendanceMetricsCalculator.calculateAttendanceRate(
                    bootcampPeriod.startDate(), today,
                    counts.absentDays(), counts.lateCount(), counts.earlyLeaveCount(), counts.outingCount(),
                    LATE_EARLY_OUTING_CONVERSION_COUNT
            );
            AttendanceRiskLevel riskLevel = AttendanceMetricsCalculator.calculateRiskLevel(rate, policy);

            result.put(userId, new StudentAttendanceRateResult(rate, riskLevel, counts));
        }
        return result;
    }
}
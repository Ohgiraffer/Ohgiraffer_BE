package com.ohgiraffer.attendance.application.cache;

import com.ohgiraffer.attendance.domain.model.AttendanceRiskLevel;
import com.ohgiraffer.attendance.domain.dto.AttendanceSummaryView;
import com.ohgiraffer.attendance.domain.dto.PeriodAttendanceRate;
import com.ohgiraffer.attendance.domain.policy.AttendanceMetricsCalculator;
import com.ohgiraffer.attendance.domain.repository.AttendanceRepository;
import com.ohgiraffer.attendance.presentation.api.response.AttendanceSummaryResponse;
import com.ohgiraffer.bootcamp.application.usecase.BootcampQueryUsecase;
import com.ohgiraffer.bootcamp.domain.model.AttendancePeriodResult;
import com.ohgiraffer.bootcamp.domain.model.AttendancePolicyResult;
import com.ohgiraffer.bootcamp.domain.model.BootcampPeriodResult;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Component
public class AttendanceSummaryCache {

    private final AttendanceRepository attendanceRepository;
    private final UserQueryUsecase userQueryUsecase;
    private final BootcampQueryUsecase bootcampQueryUsecase;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "attendanceSummary::";
    private static final Duration TTL = Duration.ofHours(25);

    // 같은 userId+date 키에 대해 동시에 여러 스레드가 캐시 미스를 겪어도
    // 실제 DB 조회/계산은 한 번만 실행되도록 막는 키 단위 락
    private final Map<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public AttendanceSummaryResponse getCachedSummary(Long userId) {
        String key = CACHE_PREFIX + userId + "-" + LocalDate.now();

        AttendanceSummaryResponse cached =
                (AttendanceSummaryResponse) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }

        ReentrantLock lock = lockMap.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            // 락을 기다리는 동안 다른 스레드가 이미 계산해서 캐시에 넣었을 수 있으므로 재확인
            cached = (AttendanceSummaryResponse) redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return cached;
            }

            AttendanceSummaryResponse result = loadFromDb(userId);
            redisTemplate.opsForValue().set(key, result, TTL);
            return result;
        } finally {
            lock.unlock();
            lockMap.remove(key, lock);
        }
    }

    private AttendanceSummaryResponse loadFromDb(Long userId) {
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

        BigDecimal attendanceRate = AttendanceMetricsCalculator.calculateAttendanceRate(
                start, end,
                summary.absentDays(), summary.lateCount(), summary.earlyLeaveCount(), summary.outingCount()
        );
        AttendanceRiskLevel riskLevel = AttendanceMetricsCalculator.calculateRiskLevel(attendanceRate, policy);

        List<PeriodAttendanceRate> periodRates = calculatePeriodRates(userId, bootcampId, today);

        return AttendanceSummaryResponse.of(summary, attendanceRate, riskLevel, periodRates);
    }

    private List<PeriodAttendanceRate> calculatePeriodRates(Long userId, Long bootcampId, LocalDate today) {
        List<AttendancePeriodResult> periods = bootcampQueryUsecase.getAttendancePeriods(bootcampId);

        return periods.stream()
                .filter(period -> !today.isBefore(period.periodStart()))
                .map(period -> {
                    LocalDate periodEnd = today.isBefore(period.periodEnd()) ? today : period.periodEnd();

                    AttendanceSummaryView periodSummary =
                            attendanceRepository.countByUserAndDateRange(userId, period.periodStart(), periodEnd);

                    BigDecimal periodRate = AttendanceMetricsCalculator.calculateAttendanceRate(
                            period.periodStart(), periodEnd,
                            periodSummary.absentDays(), periodSummary.lateCount(), periodSummary.earlyLeaveCount(), periodSummary.outingCount()
                    );

                    return new PeriodAttendanceRate(period.periodNo(), periodRate);
                })
                .toList();
    }
}
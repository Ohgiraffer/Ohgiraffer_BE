package com.ohgiraffer.attendance.application.cache;

import com.ohgiraffer.attendance.application.helper.StudentAttendanceRateResolver;
import com.ohgiraffer.attendance.domain.dto.StudentAttendanceRateResult;
import com.ohgiraffer.attendance.domain.model.AttendanceRiskLevel;
import com.ohgiraffer.attendance.domain.repository.AttendanceRepository;
import com.ohgiraffer.attendance.presentation.api.response.AttendanceDashboardSummaryResponse;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import com.ohgiraffer.user.domain.model.StudentStatusView;
import com.ohgiraffer.user.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Component
public class AttendanceDashboardCache {

    private final UserQueryUsecase userQueryUsecase;
    private final StudentAttendanceRateResolver studentAttendanceRateResolver;
    private final AttendanceRepository attendanceRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "attendanceDashboardSummary::";
    private static final Duration TTL = Duration.ofHours(25);

    private final Map<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public AttendanceDashboardSummaryResponse getCachedDashboardSummary(Long bootcampId) {
        String key = CACHE_PREFIX + bootcampId + "-" + LocalDate.now();

        AttendanceDashboardSummaryResponse cached =
                (AttendanceDashboardSummaryResponse) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }

        ReentrantLock lock = lockMap.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            cached = (AttendanceDashboardSummaryResponse) redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return cached;
            }

            AttendanceDashboardSummaryResponse result = loadFromDb(bootcampId);
            redisTemplate.opsForValue().set(key, result, TTL);
            return result;
        } finally {
            lock.unlock();
            lockMap.remove(key, lock);
        }
    }

    private AttendanceDashboardSummaryResponse loadFromDb(Long bootcampId) {
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

        // 구글 시트 동기화로 실제 출근 처리된 당일 데이터
        int attendedTodayCount = (int) attendanceRepository.countCheckedInByUserIdsAndDate(activeIds, LocalDate.now());

        // 기간 누적 데이터
        Map<Long, StudentAttendanceRateResult> rateByUserId = studentAttendanceRateResolver.resolve(bootcampId, activeIds);

        List<BigDecimal> rates = rateByUserId.values().stream()
                .map(StudentAttendanceRateResult::attendanceRate)
                .toList();

        Map<AttendanceRiskLevel, Long> riskCounts = rateByUserId.values().stream()
                .filter(r -> r.riskLevel() != null)
                .collect(Collectors.groupingBy(StudentAttendanceRateResult::riskLevel, Collectors.counting()));

        int cautionStudents = riskCounts.getOrDefault(AttendanceRiskLevel.CAUTION, 0L).intValue();
        int warningStudents = riskCounts.getOrDefault(AttendanceRiskLevel.WARNING, 0L).intValue();
        int riskStudents = riskCounts.getOrDefault(AttendanceRiskLevel.RISK, 0L).intValue();
        int atRiskStudents = cautionStudents + warningStudents + riskStudents;

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
                totalStudents, activeStudents, attendedTodayCount,
                managedStudents, cautionStudents, warningStudents, riskStudents,
                atRiskStudents, dropoutStudents
        );
    }
}
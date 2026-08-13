package com.ohgiraffer.attendance.application.cache;

import com.ohgiraffer.attendance.application.helper.StudentAttendanceRateResolver;
import com.ohgiraffer.attendance.application.port.GetUserNamesPort;
import com.ohgiraffer.attendance.domain.dto.StudentAttendanceRateResult;
import com.ohgiraffer.attendance.presentation.api.response.StudentAttendanceSummaryResponse;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Component
public class AttendanceListCache {

    private final UserQueryUsecase userQueryUsecase;
    private final GetUserNamesPort getUserNamesPort;
    private final StudentAttendanceRateResolver studentAttendanceRateResolver;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "attendanceList::";
    private static final Duration TTL = Duration.ofHours(25);

    private final Map<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public record CachedList(List<StudentAttendanceSummaryResponse> items) {}

    public CachedList getCachedSummaries(Long bootcampId) {
        String key = CACHE_PREFIX + bootcampId + "-" + LocalDate.now();

        CachedList cached = (CachedList) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }

        ReentrantLock lock = lockMap.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            // 락을 기다리는 동안 다른 스레드가 이미 계산해서 캐시에 넣었을 수 있으므로 재확인
            cached = (CachedList) redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return cached;
            }

            CachedList result = loadFromDb(bootcampId);
            redisTemplate.opsForValue().set(key, result, TTL);
            return result;
        } finally {
            lock.unlock();
            lockMap.remove(key, lock);
        }
    }

    private CachedList loadFromDb(Long bootcampId) {
        List<Long> studentIds = userQueryUsecase.getStudentIdsByBootcampId(bootcampId);
        Map<Long, String> nameByUserId = getUserNamesPort.findNamesByUserIds(studentIds);
        Map<Long, StudentAttendanceRateResult> rateByUserId = studentAttendanceRateResolver.resolve(bootcampId, studentIds);

        List<StudentAttendanceSummaryResponse> items = studentIds.stream()
                .map(userId -> {
                    String name = nameByUserId.getOrDefault(userId, "알 수 없음");
                    StudentAttendanceRateResult r = rateByUserId.get(userId);
                    return StudentAttendanceSummaryResponse.of(name, r.attendanceRate(), r.counts(), r.riskLevel());
                })
                .toList();

        return new CachedList(items);
    }
}
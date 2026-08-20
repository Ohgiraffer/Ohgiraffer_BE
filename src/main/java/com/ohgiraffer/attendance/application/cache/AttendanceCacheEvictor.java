package com.ohgiraffer.attendance.application.cache;
import com.ohgiraffer.attendance.application.usecase.AttendanceCacheEvictUsecase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class AttendanceCacheEvictor implements AttendanceCacheEvictUsecase {
    private final CacheManager cacheManager;

    public void evictSummary(Long userId) {
        evict("attendanceSummary", userId + "-" + LocalDate.now());
    }

    public void evictList(Long bootcampId) {
        evict("attendanceList", bootcampId + "-" + LocalDate.now());
    }

    public void evictDashboardSummary(Long bootcampId) {
        evict("attendanceDashboardSummary", bootcampId + "-" + LocalDate.now());
    }

    @Override
    public void evictAllForBootcamp(Long bootcampId) {
        evictList(bootcampId);
        evictDashboardSummary(bootcampId);
    }

    private void evict(String cacheName, String key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evictIfPresent(key);
            }
        } catch (Exception e) {
            log.warn("[evict] 캐시 무효화 실패, 무시하고 진행 | cacheName={}, key={}", cacheName, key, e);
        }
    }
}
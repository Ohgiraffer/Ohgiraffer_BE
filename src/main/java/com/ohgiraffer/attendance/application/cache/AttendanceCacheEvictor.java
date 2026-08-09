package com.ohgiraffer.attendance.application.cache;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class AttendanceCacheEvictor {

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

    public void evictAllForBootcamp(Long bootcampId) {
        evictList(bootcampId);
        evictDashboardSummary(bootcampId);
    }

    private void evict(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }
}
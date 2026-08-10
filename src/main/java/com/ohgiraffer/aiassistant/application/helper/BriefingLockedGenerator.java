package com.ohgiraffer.aiassistant.application.helper;

import com.ohgiraffer.aiassistant.application.port.BriefingCachePort;
import com.ohgiraffer.aiassistant.domain.model.BriefingSummary;
import com.ohgiraffer.global.aop.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class BriefingLockedGenerator {

    private final BriefingCachePort briefingCachePort;
    private final BriefingGenerator briefingGenerator;

    @DistributedLock(
            key = "'ai:briefing:lock:' + #userId",
            waitTime = 50,   // nginx 기본 60s보다 여유 있게 짧게
            leaseTime = 100,
            timeUnit = TimeUnit.SECONDS
    )
    public BriefingSummary generate(Long userId) {
        return briefingCachePort.find(userId)
                .orElseGet(() -> briefingGenerator.generateAndCache(userId));
    }

}

package com.ohgiraffer.aiassistant.application.helper;

import com.ohgiraffer.aiassistant.application.port.BriefingCachePort;
import com.ohgiraffer.aiassistant.domain.model.BriefingSummary;
import com.ohgiraffer.global.aop.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BriefingLockedGenerator {

    private final BriefingCachePort briefingCachePort;
    private final BriefingGenerator briefingGenerator;

    @DistributedLock(key = "'ai:briefing:lock:' + #userId", waitTime = 100, leaseTime = 100)
    public BriefingSummary generate(Long userId) {
        return briefingCachePort.find(userId)
                .orElseGet(() -> briefingGenerator.generateAndCache(userId));
    }

}

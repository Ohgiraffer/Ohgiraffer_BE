package com.ohgiraffer.aiassistant.application.service;

import com.ohgiraffer.aiassistant.application.helper.BriefingLockedGenerator;
import com.ohgiraffer.aiassistant.application.port.BriefingCachePort;
import com.ohgiraffer.aiassistant.application.usecae.BriefingQueryUseCase;
import com.ohgiraffer.aiassistant.domain.model.BriefingSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/* comment.
 *  BriefingQueryUseCase 구현체
 *  - 캐시 히트 시 즉시 반환(락 없이 빠른 경로)
 *  - 캐시 미스 시에만 BriefingLockedGenerator로 위임해 분산락 하에서 생성
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class BriefingQueryService implements BriefingQueryUseCase {

    private final BriefingCachePort briefingCachePort;
    private final BriefingLockedGenerator briefingLockedGenerator;

    @Override
    public BriefingSummary getBriefing(Long userId) {
        return briefingCachePort.find(userId)
                .orElseGet(() -> briefingLockedGenerator.generate(userId));
    }

}

package com.ohgiraffer.aiassistant.application.service;

import com.ohgiraffer.aiassistant.application.helper.BriefingGenerator;
import com.ohgiraffer.aiassistant.application.port.BriefingCachePort;
import com.ohgiraffer.aiassistant.application.usecae.BriefingQueryUseCase;
import com.ohgiraffer.aiassistant.domain.model.BriefingSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/* comment.
 *  BriefingQueryUseCase 구현체
 *  캐시 조회 전용, 미스 시에만 BriefingGenerator로 생성 위임 (쓰기가 섞이는 유일한 지점)
 */

@Service
@RequiredArgsConstructor
public class BriefingQueryService implements BriefingQueryUseCase {

    private final BriefingCachePort briefingCachePort;
    private final BriefingGenerator briefingGenerator;

    @Override
    public BriefingSummary getBriefing(Long userId) {
        return briefingCachePort.find(userId)
                .orElseGet(() -> briefingGenerator.generateAndCache(userId));
    }

}

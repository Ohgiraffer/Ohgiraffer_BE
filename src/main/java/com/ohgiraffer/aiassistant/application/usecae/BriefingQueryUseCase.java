package com.ohgiraffer.aiassistant.application.usecae;

import com.ohgiraffer.aiassistant.domain.model.BriefingSummary;

public interface BriefingQueryUseCase {

    // 캐시 있으면 그대로 반환, 없으면 생성 후 캐시 저장하고 반환
    BriefingSummary getBriefing(Long userId);

}

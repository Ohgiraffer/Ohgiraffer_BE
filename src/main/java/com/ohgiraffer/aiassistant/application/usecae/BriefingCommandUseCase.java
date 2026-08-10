package com.ohgiraffer.aiassistant.application.usecae;

import com.ohgiraffer.aiassistant.domain.model.BriefingSummary;

public interface BriefingCommandUseCase {

    // 캐시 무시하고 강제로 새로 생성 후 덮어쓰기
    BriefingSummary refreshBriefing(Long userId);

}

package com.ohgiraffer.aiassistant.application.service;

import com.ohgiraffer.aiassistant.application.helper.BriefingGenerator;
import com.ohgiraffer.aiassistant.application.usecae.BriefingCommandUseCase;
import com.ohgiraffer.aiassistant.domain.model.BriefingSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/* comment.
 *  BriefingCommandUseCase 구현체
 *  캐시 무시하고 항상 BriefingGenerator로 강제 재생성
 */

@Service
@RequiredArgsConstructor
public class BriefingCommandService implements BriefingCommandUseCase {

    private final BriefingGenerator briefingGenerator;

    @Override
    public BriefingSummary refreshBriefing(Long userId) {
        return briefingGenerator.generateAndCache(userId);
    }

}

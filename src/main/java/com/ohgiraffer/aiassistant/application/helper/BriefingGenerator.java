package com.ohgiraffer.aiassistant.application.helper;

import com.ohgiraffer.aiassistant.application.port.BriefingCachePort;
import com.ohgiraffer.aiassistant.application.port.BriefingDataGatheringPort;
import com.ohgiraffer.aiassistant.application.port.BriefingGenerationPort;
import com.ohgiraffer.aiassistant.domain.model.BriefingSourceData;
import com.ohgiraffer.aiassistant.domain.model.BriefingSummary;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/* comment.
 *  BriefingQueryService/BriefingCommandService가 공유하는 생성 오케스트레이션
 *  - gather(데이터 취합) -> generate(Gemini 호출) -> save(캐시 저장) 순서 고정
 *  - Query 쪽은 캐시 미스 시에만, Command 쪽은 항상 이 로직을 탐
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class BriefingGenerator {

    private final BriefingCachePort briefingCachePort;
    private final BriefingDataGatheringPort briefingDataGatheringPort;
    private final BriefingGenerationPort briefingGenerationPort;
    private final UserRepository userRepository;

    public BriefingSummary generateAndCache(Long userId) {  // package-private -> public으로 변경
        Role role = userRepository.findById(userId)
                .map(User::getRole)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        BriefingSourceData sourceData = briefingDataGatheringPort.gather(userId, role);
        String summaryText = briefingGenerationPort.generate(sourceData);

        BriefingSummary summary = new BriefingSummary(userId, summaryText, LocalDateTime.now());
        briefingCachePort.save(summary);

        log.info("[Briefing] 생성 완료 | userId={}, role={}", userId, role);

        return summary;
    }


}

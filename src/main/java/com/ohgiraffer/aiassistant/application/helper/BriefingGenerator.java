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
 *  - gather(데이터 취합) -> generate(Gemini 호출, 서킷 브레이커 적용됨) -> save(캐시 저장) 순서 고정
 *  - Query 쪽은 캐시 미스 시에만, Command 쪽은 항상 이 로직을 탐
 *  - Gemini 서킷 OPEN/장애로 인한 fallback(AI_SERVICE_UNAVAILABLE) 발생 시에는
 *    안내 메시지를 담은 BriefingSummary를 리턴하되 캐싱은 하지 않음
 *    (fallback 결과가 24시간 캐싱되어 진짜 브리핑처럼 보이는 것을 방지)
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class BriefingGenerator {

    private final BriefingCachePort briefingCachePort;
    private final BriefingDataGatheringPort briefingDataGatheringPort;
    private final BriefingGenerationPort briefingGenerationPort;
    private final UserRepository userRepository;

    // fallback 응답 시 사용할 고정 안내 문구 - 매직 스트링 반복 방지용 상수 분리
    private static final String AI_UNAVAILABLE_MESSAGE =
            "AI 비서 서비스가 일시적으로 원활하지 않습니다. 잠시 후 다시 시도해주세요.";

    public BriefingSummary generateAndCache(Long userId) {  // package-private -> public으로 변경
        Role role = userRepository.findById(userId)
                .map(User::getRole)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        BriefingSourceData sourceData = briefingDataGatheringPort.gather(userId, role);

        try {
            String summaryText = briefingGenerationPort.generate(sourceData);  // 서킷 브레이커 적용된 호출

            BriefingSummary summary = new BriefingSummary(userId, summaryText, LocalDateTime.now());
            briefingCachePort.save(summary);  // 정상 생성된 경우에만 캐싱

            log.info("[Briefing] 생성 완료 | userId={}, role={}", userId, role);

            return summary;
        } catch (BusinessException e) {
            // Gemini 서킷 OPEN 또는 API 장애로 인한 fallback인 경우 - 캐싱하지 않고 안내 응답만 리턴
            if (e.getErrorCode() == ErrorCode.AI_SERVICE_UNAVAILABLE) {
                log.warn("[Briefing] Gemini 서킷 오픈 또는 API 장애로 fallback 응답 반환 (캐싱 안 함) | userId={}", userId);
                return new BriefingSummary(userId, AI_UNAVAILABLE_MESSAGE, LocalDateTime.now());
            }
            throw e;  // AI_SERVICE_UNAVAILABLE 외의 BusinessException은 그대로 상위로 전파
        }
    }

}

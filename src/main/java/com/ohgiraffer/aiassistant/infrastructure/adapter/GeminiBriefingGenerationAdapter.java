package com.ohgiraffer.aiassistant.infrastructure.adapter;

import com.ohgiraffer.ai.infrastructure.gemini.GeminiClient;
import com.ohgiraffer.aiassistant.application.port.BriefingGenerationPort;
import com.ohgiraffer.aiassistant.domain.model.BriefingSourceData;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/* comment.
 *  BriefingGenerationPort 실구현체
 *  - BriefingPromptBuilder로 프롬프트 문자열 조립 후 기존 GeminiClient로 호출
 *  - Gemini API 호출부에 서킷 브레이커 적용 (application.yml의 "geminiApi" 인스턴스 설정 사용)
 *  - fallback은 문자열을 직접 리턴하지 않고 AI_SERVICE_UNAVAILABLE 예외를 던짐
 *    -> BriefingGenerator에서 이 예외를 캐치해 "캐싱하지 않는" 안내 응답으로 변환하도록 분리
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiBriefingGenerationAdapter implements BriefingGenerationPort {

    private final GeminiClient geminiClient;                // 기존 Gemini REST 호출 클라이언트 재사용
    private final BriefingPromptBuilder briefingPromptBuilder;

    // name은 application.yml의 resilience4j.circuitbreaker.instances.geminiApi 키와 일치해야 함
    // fallbackMethod는 원본과 파라미터 동일 + 끝에 Throwable 추가, 리턴 타입도 동일(String)해야 함
    @Override
    @CircuitBreaker(name = "geminiApi", fallbackMethod = "fallbackOnGeminiFailure")
    public String generate(BriefingSourceData sourceData) {
        String prompt = briefingPromptBuilder.build(sourceData);
        return geminiClient.generateText(prompt);
    }

    // 서킷 OPEN 상태이거나 record-exceptions에 지정된 예외 발생 시 실행
    // 문자열을 바로 리턴하면 BriefingGenerator가 이를 정상 응답으로 착각해 캐싱하므로,
    // 대신 구분 가능한 예외를 던져서 호출부(BriefingGenerator)가 캐싱 없이 처리하도록 함
    private String fallbackOnGeminiFailure(BriefingSourceData sourceData, Throwable t) {
        boolean isGeminiRelatedFailure = t instanceof RestClientException
                || t instanceof CallNotPermittedException
                || (t instanceof BusinessException be && be.getErrorCode() == ErrorCode.AI_API_CALL_FAILED);

        if (isGeminiRelatedFailure) {
            log.warn("Gemini API 호출 실패 또는 서킷 오픈 상태로 fallback 실행. userId={}, cause={}",
                    sourceData.userId(), t.toString());
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }

        // Gemini와 무관한 예외는 원인을 숨기지 않고 그대로 전파
        log.error("Gemini 호출 경로에서 예상치 못한 예외 발생 - fallback 대상 아님. userId={}",
                sourceData.userId(), t);
        if (t instanceof RuntimeException re) {
            throw re;
        }
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, t);
    }
}

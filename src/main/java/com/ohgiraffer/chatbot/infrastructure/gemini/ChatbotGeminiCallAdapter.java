package com.ohgiraffer.chatbot.infrastructure.gemini;

import com.ohgiraffer.ai.infrastructure.gemini.GeminiClient;
import com.ohgiraffer.chatbot.domain.model.ChatbotGeminiTurnResult;
import com.ohgiraffer.chatbot.domain.model.ChatbotGeminiFunctionCall;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * comment.
 *  챗봇 전용 Gemini 호출 어댑터
 *  - GeminiClient.generateWithTools()를 감싸서 서킷 브레이커(geminiApiChatbot 인스턴스) 적용
 *  - raw 응답을 parts 순회하며 functionCall/text로 분리해 ChatbotGeminiTurnResult로 변환
 *  - fallback은 브리핑과 별개 메서드 (파라미터/리턴 타입이 달라 재사용 불가, resilience4j 제약사항)
 */

@Slf4j
@Component
public class ChatbotGeminiCallAdapter {

    private final GeminiClient geminiClient;

    public ChatbotGeminiCallAdapter(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    @SuppressWarnings("unchecked")
    @CircuitBreaker(name = "geminiApiChatbot", fallbackMethod = "fallbackOnChatbotGeminiFailure")
    public ChatbotGeminiTurnResult call(List<Map<String, Object>> contents, List<Map<String, Object>> tools) {
        Map<String, Object> response = geminiClient.generateWithTools(contents, tools);

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new BusinessException(ErrorCode.AI_API_CALL_FAILED);
        }

        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        if (content == null) {
            throw new BusinessException(ErrorCode.AI_API_CALL_FAILED);
        }

        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        if (parts == null) {
            throw new BusinessException(ErrorCode.AI_API_CALL_FAILED);
        }

        List<ChatbotGeminiFunctionCall> functionCalls = new ArrayList<>();
        StringBuilder textBuilder = new StringBuilder();

        for (Map<String, Object> part : parts) {
            if (part.containsKey("functionCall")) {
                Map<String, Object> fc = (Map<String, Object>) part.get("functionCall");
                // thoughtSignature는 functionCall 객체 안이 아니라 part와 같은 레벨의 형제 필드로 옴 (Gemini 3.x 스펙)
                String thoughtSignature = (String) part.get("thoughtSignature");
                functionCalls.add(new ChatbotGeminiFunctionCall(
                        (String) fc.get("id"), // Gemini 3.x부터 응답에 포함 - functionResponse 돌려줄 때 그대로 echo해야 함
                        (String) fc.get("name"),
                        (Map<String, Object>) fc.getOrDefault("args", Map.of()),
                        thoughtSignature
                ));
            } else if (part.containsKey("text")) {
                textBuilder.append((String) part.get("text"));
            }
        }

        return new ChatbotGeminiTurnResult(functionCalls, textBuilder.isEmpty() ? null : textBuilder.toString());
    }

    // 서킷 OPEN 또는 Gemini 관련 장애 시 - 하드코딩된 안내 텍스트를 최종 답변으로 반환 (에러 처리 A안: 즉시 응답, functionResponse 왕복 없음)
    private ChatbotGeminiTurnResult fallbackOnChatbotGeminiFailure(List<Map<String, Object>> contents, List<Map<String, Object>> tools, Throwable t) {
        boolean isGeminiRelatedFailure = t instanceof RestClientException
                || t instanceof CallNotPermittedException
                || (t instanceof BusinessException be && be.getErrorCode() == ErrorCode.AI_API_CALL_FAILED);

        if (isGeminiRelatedFailure) {
            log.warn("[ChatbotGemini] API 호출 실패 또는 서킷 오픈으로 fallback 실행 | cause={}", t.toString());
            return new ChatbotGeminiTurnResult(List.of(), "AI비서 응답이 원활하지 않습니다. 잠시 후 다시 시도해주세요.");
        }

        log.error("[ChatbotGemini] 예상치 못한 예외 발생 - fallback 대상 아님", t);
        if (t instanceof RuntimeException re) {
            throw re;
        }
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, t);
    }

}

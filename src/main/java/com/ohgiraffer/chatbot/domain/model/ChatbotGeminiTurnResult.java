package com.ohgiraffer.chatbot.domain.model;

import java.util.List;

/* comment.
 *  Gemini 응답 1턴을 파싱한 결과
 *  - functionCalls가 비어있지 않으면 함수 실행 필요, 비어있으면 finalText가 최종 답변
 *  - Gemini가 병렬로 여러 functionCall을 동시에 요청하는 경우까지 대비해 List로 설계
 */

public record ChatbotGeminiTurnResult(
        List<ChatbotGeminiFunctionCall> functionCalls,
        String finalText
) {

    public boolean hasFunctionCalls() {
        return functionCalls != null && !functionCalls.isEmpty();
    }

}

package com.ohgiraffer.chatbot.domain.model;

import java.time.LocalDateTime;
import java.util.List;

/* comment.
 *  Gemini function-calling 대화 히스토리 저장 단위
 *  - role: "user" / "model" / "function" (Gemini contents 배열 규격 그대로)
 *  - parts: 텍스트, functionCall, functionResponse 중 하나를 담는 원시 구조 (Map 기반, Jackson 직렬화 대상)
 */

public record ChatbotSessionTurn(
        String role,
        List<Object> parts,
        LocalDateTime createdAt
) {
}

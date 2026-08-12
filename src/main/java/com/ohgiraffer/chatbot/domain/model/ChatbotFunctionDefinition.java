package com.ohgiraffer.chatbot.domain.model;

import com.ohgiraffer.user.domain.model.Role;

import java.util.Map;
import java.util.Set;

/*
 * comment.
 *  Gemini function-calling에 노출할 함수 1개의 스펙
 *  - name/description/parameterSchema는 Gemini functionDeclarations 규격 그대로 매핑됨
 *  - allowedRoles: 이 role 집합에 없으면 tools 목록 자체에서 제외됨 (사후검증이 아니라 사전 비노출 방식)
 */

public record ChatbotFunctionDefinition(
        String name,
        String description,
        Map<String, Object> parameterSchema, // Gemini Schema 규격 (type/properties/required)
        Set<Role> allowedRoles
) {

    // 파라미터 없는 함수용 - 빈 OBJECT 스키마 고정 생성
    public static Map<String, Object> noParams() {
        return Map.of("type", "OBJECT", "properties", Map.of());
    }

}

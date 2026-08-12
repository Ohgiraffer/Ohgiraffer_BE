package com.ohgiraffer.chatbot.domain.model;

import java.util.Map;

/* comment.
 *  Gemini가 요청한 단일 함수 호출 정보 (functionCall 파트 파싱 결과)
 *  - id: Gemini 3.x부터 응답에 포함되는 호출 식별자. functionResponse를 돌려줄 때
 *    이 id를 그대로 echo해야 모델이 어느 호출에 대한 응답인지 매칭할 수 있음 (3.x 필수 요구사항)
 *  - thoughtSignature: 모델의 사고 과정을 나타내는 서명값. 병렬 함수 호출 시 첫 번째
 *    functionCall part에만 존재(나머지는 null). 다음 턴 요청에 그대로 재전송해야
 *    모델이 이전 추론 문맥을 이어갈 수 있음
 */

public record ChatbotGeminiFunctionCall(
        String id,
        String name,
        Map<String, Object> args,
        String thoughtSignature
) {
}

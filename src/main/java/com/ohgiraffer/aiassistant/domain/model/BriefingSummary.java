package com.ohgiraffer.aiassistant.domain.model;

import java.time.Instant;

/* comment.
 *  AI 브리핑 캐시/응답에 쓰이는 값 객체
 *  - Redis에 JSON 직렬화되어 저장되는 최소 단위, DB 미영속
 */

public record BriefingSummary(
        Long userId,              // 브리핑 대상 유저
        String summaryText,       // Gemini가 생성한 마크다운 브리핑 본문
        Instant generatedAt // 생성 시각
) {
}

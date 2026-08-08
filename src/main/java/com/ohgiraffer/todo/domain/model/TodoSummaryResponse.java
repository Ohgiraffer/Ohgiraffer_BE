package com.ohgiraffer.todo.domain.model;

import java.time.LocalDateTime;

/* comment.
 *  역할별 요약 응답에 쓰이는 값 객체.
 *  - 각 Port의 getSummary()가 반환하는 단위
 *  - "미제출 발표자료 3건"처럼 대시보드 카드 한 줄에 대응
 */

public record TodoSummaryResponse(
        TodoSourceDomain sourceDomain,  // 어느 도메인 건인지 식별자
        String type,                    // 세부 타입 (예: 발표자료, 결재대기 등)
        long count,                     // 대기 건수
        LocalDateTime nearestDueTime    // 가장 임박한 마감/예정 시각 (nullable)
) {
}

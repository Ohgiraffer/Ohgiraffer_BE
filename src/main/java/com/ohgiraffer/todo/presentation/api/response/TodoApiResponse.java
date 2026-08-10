package com.ohgiraffer.todo.presentation.api.response;

import com.ohgiraffer.todo.domain.model.TodoSourceDomain;

import java.time.LocalDateTime;

/* comment.
 *  역할별 요약 엔드포인트 응답 DTO
 *  - domain.model.TodoSummaryResponse를 그대로 노출하지 않고 presentation 계층에서 별도로 감싸서 응답 계약을 분리
 */

public record TodoApiResponse(
        TodoSourceDomain sourceDomain,  // 어느 도메인 건인지 식별자
        String type,                    // 세부 타입
        long count,                     // 대기 건수
        LocalDateTime nearestDueTime    // 가장 임박한 마감/예정 시각 (nullable)
) {
}

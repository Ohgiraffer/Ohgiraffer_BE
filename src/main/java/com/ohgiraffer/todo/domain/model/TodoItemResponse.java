package com.ohgiraffer.todo.domain.model;

import java.time.LocalDateTime;

/* comment.
 *  TODO 도메인의 공통 응답 값 객체
 *  - 6개 Port(SubmissionTodoPort 등)가 getPendingItems()에서 공통으로 반환하는 항목 단위
 *  - TodoQueryService가 6개 Port 결과를 이 타입 리스트로 취합해서 응답 조립
 *  - AI비서 BriefingDataGatheringAdapter도 이 타입을 그대로 재사용
 */

public record TodoItemResponse(
        TodoSourceDomain sourceDomain,  // 어느 도메인 건인지 식별자
        Long itemId,                    // 원본 도메인 PK
        String type,                    // 도메인 내 세부 타입 (예: 발표자료, 휴가신청, 상담예정 등)
        String status,                  // 도메인별 원본 상태값 그대로 (공통 enum 미변환)
        LocalDateTime createdAt,        // 생성 시각
        LocalDateTime dueOrEventTime    // 마감/예정 시각 (nullable - 상담·제출물처럼 시간 있는 항목만 채움)
) {
}

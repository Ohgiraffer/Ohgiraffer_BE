package com.ohgiraffer.chat.domain.repository;

import java.time.LocalDateTime;

/*
 * comment.
 *  채팅 메시지 통합 검색 조회 조건
 *  컨트롤러의 요청 파라미터를 그대로 쓰지 않고, 저장소가 이해하는 형태로 별도 정의함
 *  -> presentation 계층(request DTO)이 바뀌어도 domain/repository는 영향받지 않게 분리
 */

public record ChatMessageSearchCondition(
        String channelId,   // 검색 대상 채널 (null이면 전체 채널 대상)
        Long senderId,      // 특정 작성자로 필터링 (null이면 전체)
        String keyword,     // 메시지 내용 키워드 (null/blank면 조건 미적용)
        LocalDateTime startDate,  // 검색 시작일시 (null이면 하한 없음)
        LocalDateTime endDate     // 검색 종료일시 (null이면 상한 없음)
) {
}

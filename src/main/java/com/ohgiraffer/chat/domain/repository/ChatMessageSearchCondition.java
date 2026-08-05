package com.ohgiraffer.chat.domain.repository;

import java.time.Instant;
import java.util.List;

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
        Instant startDate,  // 검색 시작일시 (null이면 하한 없음)
        Instant endDate,     // 검색 종료일시 (null이면 상한 없음)
        // channelId 미지정 검색 시 내가 속한 채널로만 범위를 제한하는 화이트리스트 (null이면 제한 없음)
        List<String> allowedChannelIds
) {

    // 컨트롤러에서 channelId/senderId/keyword/startDate/endDate 5개만 넘길 때 쓰는 생성자
    // allowedChannelIds는 null(제한 없음)로 채움 - service 계층에서 필요 시 재조립함
    public ChatMessageSearchCondition(String channelId, Long senderId, String keyword, Instant startDate, Instant endDate) {
        this(channelId, senderId, keyword, startDate, endDate, null);
    }

    // 기존 조건(condition)에 allowedChannelIds만 덮어씌운 새 인스턴스 생성용
    // channelId 미지정 검색 시 내가 속한 채널로 범위 제한할 때 씀 (IDOR 방지)
    public ChatMessageSearchCondition(ChatMessageSearchCondition base, List<String> allowedChannelIds) {
        this(base.channelId(), base.senderId(), base.keyword(), base.startDate(), base.endDate(), allowedChannelIds);
    }

}

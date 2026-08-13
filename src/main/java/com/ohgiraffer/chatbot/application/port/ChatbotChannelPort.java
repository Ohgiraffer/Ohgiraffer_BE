package com.ohgiraffer.chatbot.application.port;

import java.util.Optional;

/*
 * comment.
 *  유저별 AI비서 전용 채널 URL 매핑 포트
 *  - RedisBriefingCacheAdapter와 동일 성격의 단일 값 캐시 계약
 */

public interface ChatbotChannelPort {

    // 저장된 채널 URL 조회 - 없으면 empty
    Optional<String> findChannelUrl(Long userId);

    // 채널 URL 저장
    void saveChannelUrl(Long userId, String channelUrl);

    // 특정 channelUrl이 AI비서 채널인지 여부 확인 - 웹훅 미동작으로 인한 직접 트리거 분기에서 사용
    boolean isChatbotChannel(String channelUrl);

}

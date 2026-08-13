package com.ohgiraffer.chatbot.application.usecase;

/*
 * comment.
 *  AI비서 채널 조회 계약 (Query)
 *  - 없으면 생성 후 반환하는 멱등 동작이지만, 유저 관점에서는 "조회" 요청이라 Query로 분류
 */

public interface ChatbotChannelQueryUseCase {

    // 유저의 AI비서 채널 URL 조회 - 없으면 생성 후 반환
    String getChannelUrl(Long userId);

}

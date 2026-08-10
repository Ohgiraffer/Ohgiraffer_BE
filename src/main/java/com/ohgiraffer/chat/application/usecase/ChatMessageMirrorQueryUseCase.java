package com.ohgiraffer.chat.application.usecase;

import com.ohgiraffer.chat.application.result.ChatMessageResult;
import com.ohgiraffer.chat.domain.repository.ChatMessageSearchCondition;
import com.ohgiraffer.chat.presentation.api.response.ReplyCountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/*
 * comment.
 *  채팅 메시지 조회 계약
 */

public interface ChatMessageMirrorQueryUseCase {

    // 채널 메시지 이력 조회
    Page<ChatMessageResult> getChannelMessages(String channelId, Long principalId, Pageable pageable);

    // 스레드 답글 조회 - parentSendbirdMessageId(String, 클라이언트가 응답에서 받은 값)로 원본 메시지 특정
    Page<ChatMessageResult> getThreadReplies(String parentSendbirdMessageId, Long principalId, Pageable pageable);

    // 메시지 통합 검색
    Page<ChatMessageResult> searchMessages(ChatMessageSearchCondition condition, Long principalId, Pageable pageable);

    // 특정 메시지(parentMessageId)에 달린 답글 개수 단건 조회 - principalId로 소속 채널 멤버십 검증
    ReplyCountResponse getReplyCount(String messageId, Long principalId);

}

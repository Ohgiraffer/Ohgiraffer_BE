package com.ohgiraffer.chat.application.usecase;

import com.ohgiraffer.chat.application.result.ChatMessageResult;
import com.ohgiraffer.chat.domain.repository.ChatMessageSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/*
 * comment.
 *  채팅 메시지 조회 계약
 */

public interface ChatMessageMirrorQueryUseCase {

    // 채널 메시지 이력 조회
    List<ChatMessageResult> getChannelMessages(String channelId);

    // 스레드 답글 조회
    List<ChatMessageResult> getThreadReplies(Long parentMessageId);

    // 메시지 통합 검색
    Page<ChatMessageResult> searchMessages(ChatMessageSearchCondition condition, Pageable pageable);


}

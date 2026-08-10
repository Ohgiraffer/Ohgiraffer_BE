package com.ohgiraffer.chat.application.result;

import java.time.Instant;

/*
 * comment.
 *  메시지 조회 결과 (채널 이력, 스레드 답글, 통합검색 공용)
 *  ChatMessageMirror 도메인 필드를 그대로 노출
 */

public record ChatMessageResult(
        Long id,
        String channelId,
        String sendbirdMessageId,
        Long parentMessageId,
        Long senderId,
        String content,
        String attachmentUrl,
        String attachmentType,
        Instant sentAt,
        boolean isEdited,
        Long unreadCount
) {

    // 안읽음 수 계산이 필요 없는 조회(스레드 답글)용 - unreadCount는 항상 null
    public static ChatMessageResult from(com.ohgiraffer.chat.domain.model.ChatMessageMirror domain) {
        return from(domain, null);
    }

    public static ChatMessageResult from(com.ohgiraffer.chat.domain.model.ChatMessageMirror domain, Long unreadCount) {
        return new ChatMessageResult(
                domain.getId(), domain.getChannelId(), domain.getSendbirdMessageId(),
                domain.getParentMessageId(), domain.getSenderId(), domain.getContent(), domain.getAttachmentUrl(),
                domain.getAttachmentType(), domain.getSentAt(), domain.isEdited(), unreadCount
        );
    }

}

package com.ohgiraffer.chat.infrastructure.persistence;

import com.ohgiraffer.chat.domain.model.ChatChannelMember;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/*
 * comment.
 *  DB 테이블(chat_channel_member)과 1:1 매핑되는 JPA 클래스
 *  -> Domain Model(ChatChannelMember)을 모르고 DB 컬럼 구조만 표현
 */

@Getter
@Entity
@Table(name = "chat_channel_member")
@NoArgsConstructor
public class ChatChannelMemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_channel_member_id")
    private Long id;

    @Column(name = "chat_channel_id", nullable = false)
    private Long chatChannelId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    @Column(name = "last_read_at")
    private Instant lastReadAt;

    // Domain -> JpaEntity 변환 (저장용)
    public static ChatChannelMemberJpaEntity from(ChatChannelMember domain) {
        ChatChannelMemberJpaEntity entity = new ChatChannelMemberJpaEntity();
        entity.id = domain.getId();
        entity.chatChannelId = domain.getChatChannelId();
        entity.userId = domain.getUserId();
        entity.joinedAt = domain.getJoinedAt();
        entity.leftAt = domain.getLeftAt();
        entity.lastReadMessageId = domain.getLastReadMessageId();
        entity.lastReadAt = domain.getLastReadAt();
        return entity;
    }

    // JpaEntity -> Domain 변환 (조회용)
    public ChatChannelMember toDomain() {
        return ChatChannelMember.reconstitute(
                id, chatChannelId, userId, joinedAt, leftAt, lastReadMessageId, lastReadAt
        );
    }

}

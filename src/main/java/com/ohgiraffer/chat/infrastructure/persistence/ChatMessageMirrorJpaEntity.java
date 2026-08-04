package com.ohgiraffer.chat.infrastructure.persistence;

import com.ohgiraffer.chat.domain.model.ChatMessageMirror;
import com.ohgiraffer.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

/*
 * comment.
 *  DB 테이블(chat_message_mirror)과 1:1 매핑되는 JPA 클래스
 *  -> Domain Model(ChatMessageMirror)을 모르고 DB 컬럼 구조만 표현
 *  -> 변환은 ChatMessageMirrorRepositoryAdapter가 담당
 */

@Getter
@Entity
@Table(name = "chat_message_mirror")
@NoArgsConstructor
public class ChatMessageMirrorJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long id;

    @Column(name = "channel_id", nullable = false, length = 100)
    private String channelId;

    @Column(name = "sendbird_message_id", nullable = false, length = 100)
    private String sendbirdMessageId;

    @Column(name = "parent_message_id")
    private Long parentMessageId;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "attachment_type", length = 30)
    private String attachmentType;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_edited", nullable = false)
    private boolean isEdited;

    // Domain -> JpaEntity 변환 (저장용)
    public static ChatMessageMirrorJpaEntity from(ChatMessageMirror domain) {
        ChatMessageMirrorJpaEntity entity = new ChatMessageMirrorJpaEntity();
        entity.id = domain.getId();
        entity.channelId = domain.getChannelId();
        entity.sendbirdMessageId = domain.getSendbirdMessageId();
        entity.parentMessageId = domain.getParentMessageId();
        entity.senderId = domain.getSenderId();
        entity.content = domain.getContent();
        entity.attachmentUrl = domain.getAttachmentUrl();
        entity.attachmentType = domain.getAttachmentType();
        entity.sentAt = domain.getSentAt();
        entity.deletedAt = domain.getDeletedAt();
        entity.isEdited = domain.isEdited();
        return entity;
    }

    // JpaEntity -> Domain 변환 (조회용)
    public ChatMessageMirror toDomain() {
        return ChatMessageMirror.reconstitute(
                id, channelId, sendbirdMessageId, parentMessageId, senderId,
                content, attachmentUrl, attachmentType, sentAt, deletedAt, isEdited,
                getCreatedAt(), getUpdatedAt()
        );
    }

}

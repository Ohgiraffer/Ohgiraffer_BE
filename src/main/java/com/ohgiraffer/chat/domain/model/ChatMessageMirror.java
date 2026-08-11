package com.ohgiraffer.chat.domain.model;

import java.time.Instant;
import java.time.LocalDateTime;

/*
 * comment.
 *  채팅 메시지 미러링 도메인 모델
 *  JPA를 모르는 순수 객체 - 영속성 표현(ChatMessageMirrorJpaEntity)과 분리됨
 *  -> 변환은 ChatMessageMirrorRepositoryAdapter가 담당
 */

public class ChatMessageMirror {

    private final Long id;
    private final String channelId;
    private final String sendbirdMessageId;
    private final Long parentMessageId;
    private final Long senderId;
    private String content;
    private String attachmentUrl;
    private final String attachmentType;
    private final Instant sentAt;
    private LocalDateTime deletedAt;
    private boolean isEdited;
    private Instant lastEventAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    private ChatMessageMirror(Long id, String channelId, String sendbirdMessageId, Long parentMessageId,
                              Long senderId, String content, String attachmentUrl, String attachmentType,
                              Instant sentAt, LocalDateTime deletedAt, boolean isEdited, Instant lastEventAt,
                              Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.channelId = channelId;
        this.sendbirdMessageId = sendbirdMessageId;
        this.parentMessageId = parentMessageId;
        this.senderId = senderId;
        this.content = content;
        this.attachmentUrl = attachmentUrl;
        this.attachmentType = attachmentType;
        this.sentAt = sentAt;
        this.deletedAt = deletedAt;
        this.isEdited = isEdited;
        this.lastEventAt = lastEventAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 신규 메시지 생성 (웹훅 미러링, id/deletedAt/isEdited는 아직 없음)
    public static ChatMessageMirror create(String channelId, String sendbirdMessageId, Long parentMessageId,
                                           Long senderId, String content, String attachmentUrl,
                                           String attachmentType, Instant sentAt) {
        return new ChatMessageMirror(
                null, channelId, sendbirdMessageId, parentMessageId, senderId,
                content, attachmentUrl, attachmentType, sentAt, null, false, sentAt,null, null
        );
    }

    // DB에서 조회한 값으로 도메인 객체 복원 (JpaEntity.toDomain()에서 사용)
    public static ChatMessageMirror reconstitute(Long id, String channelId, String sendbirdMessageId,
                                                 Long parentMessageId, Long senderId, String content,
                                                 String attachmentUrl, String attachmentType,
                                                 Instant sentAt, LocalDateTime deletedAt, boolean isEdited,
                                                 Instant lastEventAt, Instant createdAt, Instant updatedAt) {
        return new ChatMessageMirror(
                id, channelId, sendbirdMessageId, parentMessageId, senderId,  content, attachmentUrl,
                attachmentType, sentAt, deletedAt, isEdited, lastEventAt, createdAt, updatedAt
        );
    }

    // 이 이벤트가 이미 반영된 것보다 오래된 이벤트인지 판단
    // lastEventAt이 없는(과거 데이터) 경우는 비교 불가하므로 최신으로 간주하고 반영 허용
    public boolean isOlderEventThan(Instant eventAt) {
        Instant baseline = (this.lastEventAt != null) ? this.lastEventAt : this.sentAt;
        return eventAt.isBefore(baseline);
    }

    public void edit(String newContent, String newAttachmentUrl, Instant eventAt) {
        this.content = newContent;
        this.attachmentUrl = newAttachmentUrl;
        this.isEdited = true;
        this.lastEventAt = eventAt;
    }

    public void delete(Instant eventAt) {
        if (this.deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
        }
        this.lastEventAt = eventAt;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public Long getId() { return id; }
    public String getChannelId() { return channelId; }
    public String getSendbirdMessageId() { return sendbirdMessageId; }
    public Long getParentMessageId() { return parentMessageId; }
    public Long getSenderId() { return senderId; }
    public String getContent() { return content; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public String getAttachmentType() { return attachmentType; }
    public Instant getSentAt() { return sentAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public boolean isEdited() { return isEdited; }
    public Instant getLastEventAt() { return lastEventAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

}

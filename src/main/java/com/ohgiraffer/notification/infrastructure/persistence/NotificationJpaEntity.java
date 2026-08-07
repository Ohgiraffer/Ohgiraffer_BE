package com.ohgiraffer.notification.infrastructure.persistence;

import com.ohgiraffer.notification.domain.model.Notification;
import com.ohgiraffer.notification.domain.model.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(name = "notification")
@NoArgsConstructor
public class NotificationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    // 수신자 사용자 아이디
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 알림 유형 (9종 ENUM)
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    // 알림 제목
    @Column(name = "title", nullable = false)
    private String title;

    // 알림 본문
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // 관련 엔티티 유형 - 폴리모픽, FK 없음
    @Column(name = "related_entity_type")
    private String relatedEntityType;

    // 관련 엔티티 아이디 - 폴리모픽, FK 없음
    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    // 읽음 여부
    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    // 생성(수신)일시 - 최신순 정렬 기준, insert 시 1회만 세팅되고 이후 불변
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Domain -> JpaEntity 변환 (저장용)
    public static NotificationJpaEntity from(Notification domain) {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.id = domain.getNotificationId();
        entity.userId = domain.getUserId();
        entity.notificationType = domain.getNotificationType();
        entity.title = domain.getTitle();
        entity.content = domain.getContent();
        entity.relatedEntityType = domain.getRelatedEntityType();
        entity.relatedEntityId = domain.getRelatedEntityId();
        entity.isRead = domain.isRead();
        entity.createdAt = domain.getCreatedAt() != null ? domain.getCreatedAt() : Instant.now();
        return entity;
    }

    // JpaEntity -> Domain 변환 (조회용)
    public Notification toDomain() {
        return new Notification(
                id, userId, notificationType, title, content,
                relatedEntityType, relatedEntityId, isRead, createdAt
        );
    }

}

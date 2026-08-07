package com.ohgiraffer.notification.domain.model;

import java.time.Instant;

/*
 * comment.
 *  알림 도메인 모델 (순수 POJO, JPA 의존성 없음)
 *  - notification 테이블 1개를 표현하며, TODO처럼 파생조회가 아니라 자체 저장소를 가진 도메인
 *  - 하드 딜리트 정책이라 deletedAt 없음, 수정 개념이 없는 도메인이라 updatedAt 없이 createdAt만 관리
 *  - 생성은 각 도메인(전자결재/공지/캘린더/출결/상담/제출/채팅)의 내부 트리거로만 이루어짐
 *  - 상태 변경은 읽음 처리(markAsRead) 하나뿐이고, 삭제는 도메인 행위가 아니라 Repository 단에서 직접 처리
 * */

public class Notification {

    // 알림 PK
    private final Long notificationId;

    // 알림 수신자 사용자 아이디
    private final Long userId;

    // 알림 유형 (9종 ENUM)
    private final NotificationType notificationType;

    // 알림 제목 (Figma 확인 후 신규 확정된 필드)
    private final String title;

    // 알림 본문 내용
    private final String content;

    // 관련 엔티티 유형 (폴리모픽, FK 없음. 예: APPROVAL, CHAT_CHANNEL 등)
    private final String relatedEntityType;

    // 관련 엔티티 아이디 (폴리모픽, FK 없음)
    private final Long relatedEntityId;

    // 읽음 여부
    private boolean isRead;

    // 생성(수신)일시, 최신순 정렬 기준
    private final Instant createdAt;

    /**
     * 기존 데이터를 복원할 때 사용하는 전체 필드 생성자
     * Repository가 DB에서 조회한 값을 도메인 객체로 변환할 때 사용함
     *
     * @param notificationId    알림 PK
     * @param userId            수신자 아이디
     * @param notificationType  알림 유형
     * @param title             알림 제목
     * @param content           알림 본문
     * @param relatedEntityType 관련 엔티티 유형 (폴리모픽)
     * @param relatedEntityId   관련 엔티티 아이디 (폴리모픽)
     * @param isRead            읽음 여부
     * @param createdAt         생성일시
     */
    public Notification(
            Long notificationId, Long userId, NotificationType notificationType, String title,
            String content, String relatedEntityType, Long relatedEntityId, boolean isRead, Instant createdAt
    ) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.notificationType = notificationType;
        this.title = title;
        this.content = content;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    /**
     * 신규 알림 생성 시 사용하는 정적 팩토리 메서드 (NOTI-008 내부 생성 API용)
     * notificationId와 createdAt은 아직 저장 전이라 비워두고, 저장 시점에 DB/Auditing이 채움
     *
     * @param userId            수신자 아이디
     * @param notificationType  알림 유형
     * @param title             알림 제목
     * @param content           알림 본문
     * @param relatedEntityType 관련 엔티티 유형
     * @param relatedEntityId   관련 엔티티 아이디
     * @return 저장 전 상태의 신규 Notification 도메인 객체
     */
    public static Notification create(
            Long userId, NotificationType notificationType, String title,
            String content, String relatedEntityType, Long relatedEntityId
    ) {
        return new Notification(null, userId, notificationType, title, content,
                relatedEntityType, relatedEntityId, false, null
        );
    }

    /**
     * 알림을 읽음 상태로 변경 (NOTI-002)
     * 이미 읽은 알림에 다시 호출해도 안전하게 동작함 (멱등)
     */
    public void markAsRead() {this.isRead = true;}

    /**
     * 이 알림이 특정 유저 소유인지 검증 (NOTI-002/003 접근 권한 체크용)
     * false 반환 시 서비스 계층에서 NOTI_002(접근 권한 없음) 예외로 변환함
     *
     * @param userId 검증할 유저 아이디
     * @return 본인 소유면 true
     */
    public boolean isOwnedBy(Long userId) {return this.userId.equals(userId);}
    public Long getNotificationId() {return notificationId;}
    public Long getUserId() {return userId;}
    public NotificationType getNotificationType() {return notificationType;}
    public String getTitle() {return title;}
    public String getContent() {return content;}
    public String getRelatedEntityType() {return relatedEntityType;}
    public Long getRelatedEntityId() {return relatedEntityId;}
    public boolean isRead() {return isRead;}
    public Instant getCreatedAt() {return createdAt;}

}

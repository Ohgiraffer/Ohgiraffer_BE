package com.ohgiraffer.notice.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * 공지 확인 기록.
 *
 * <p>테이블에 reaction_emoji 컬럼이 남아 있으나 매핑하지 않는다.
 * 확인 방식을 체크박스로 통일하면서 이모지를 쓰지 않게 되었고, ERD 정리 대상이다.
 */
@Entity
@Table(name = "notice_confirmation")
@IdClass(NoticeConfirmationId.class)
public class NoticeConfirmationJpaEntity {

    @Id
    @Column(name = "notice_id")
    private Long noticeId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "confirmed_at", nullable = false)
    private Instant confirmedAt;

    protected NoticeConfirmationJpaEntity() {
    }

    private NoticeConfirmationJpaEntity(
            Long noticeId,
            Long userId,
            Instant confirmedAt
    ) {
        this.noticeId = noticeId;
        this.userId = userId;
        this.confirmedAt = confirmedAt;
    }

    public static NoticeConfirmationJpaEntity of(
            Long noticeId,
            Long userId,
            Instant confirmedAt
    ) {
        return new NoticeConfirmationJpaEntity(noticeId, userId, confirmedAt);
    }

    public Long getNoticeId() {
        return noticeId;
    }
}

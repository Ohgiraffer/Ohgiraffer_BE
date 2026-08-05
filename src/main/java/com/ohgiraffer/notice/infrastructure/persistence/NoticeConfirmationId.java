package com.ohgiraffer.notice.infrastructure.persistence;

import java.io.Serializable;
import java.util.Objects;

/**
 * notice_confirmation 의 복합 기본키 (notice_id, user_id).
 */
public class NoticeConfirmationId implements Serializable {

    private Long noticeId;
    private Long userId;

    protected NoticeConfirmationId() {
    }

    public NoticeConfirmationId(Long noticeId, Long userId) {
        this.noticeId = noticeId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof NoticeConfirmationId that)) {
            return false;
        }

        return Objects.equals(noticeId, that.noticeId)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noticeId, userId);
    }
}

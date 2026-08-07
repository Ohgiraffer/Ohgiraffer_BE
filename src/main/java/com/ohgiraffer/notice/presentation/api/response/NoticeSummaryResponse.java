package com.ohgiraffer.notice.presentation.api.response;

import com.ohgiraffer.notice.application.query.NoticeSummaryView;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 공지 목록의 한 줄. 시각은 서버에서 한국 시간으로 변환해 내려준다.
 */
public record NoticeSummaryResponse(
        Long noticeId,
        Long categoryId,
        String categoryName,
        String title,
        Long authorId,
        String authorName,
        boolean pinned,
        boolean confirmedByMe,
        LocalDateTime createdAt
) {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static NoticeSummaryResponse from(NoticeSummaryView view) {
        return new NoticeSummaryResponse(
                view.noticeId(),
                view.categoryId(),
                view.categoryName(),
                view.title(),
                view.authorId(),
                view.authorName(),
                view.pinned(),
                view.confirmedByMe(),
                toKst(view.createdAt())
        );
    }

    private static LocalDateTime toKst(Instant instant) {
        if (instant == null) {
            return null;
        }

        return instant.atZone(KST).toLocalDateTime();
    }
}

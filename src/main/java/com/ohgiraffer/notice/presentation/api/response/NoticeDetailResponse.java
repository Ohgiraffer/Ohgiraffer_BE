package com.ohgiraffer.notice.presentation.api.response;

import com.ohgiraffer.notice.application.query.NoticeDetailView;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 공지 상세 조회 응답. 시각은 한국 시간으로 변환해 내려준다.
 *
 * <p>확인 체크박스는 고정 여부와 무관하게 모든 공지에 노출된다.
 * {@code pinned} 는 목록 상단 고정 여부일 뿐이다.
 */
public record NoticeDetailResponse(
        Long noticeId,
        Long categoryId,
        String categoryName,
        String title,
        String content,
        Long authorId,
        String authorName,
        boolean pinned,
        boolean visibleToTrainee,
        long confirmationCount,
        boolean confirmedByMe,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static NoticeDetailResponse from(NoticeDetailView view) {
        return new NoticeDetailResponse(
                view.noticeId(),
                view.categoryId(),
                view.categoryName(),
                view.title(),
                view.content(),
                view.authorId(),
                view.authorName(),
                view.pinned(),
                view.visibleToTrainee(),
                view.confirmationCount(),
                view.confirmedByMe(),
                toKst(view.createdAt()),
                toKst(view.updatedAt())
        );
    }

    private static LocalDateTime toKst(Instant instant) {
        if (instant == null) {
            return null;
        }

        return instant.atZone(KST).toLocalDateTime();
    }
}

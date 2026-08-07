package com.ohgiraffer.notice.presentation.api.response;

import com.ohgiraffer.notice.application.query.NoticeDetailView;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

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
        List<NoticeAttachmentResponse> attachments,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /**
     * @param attachments 다운로드 주소까지 채워 넣은 첨부 목록.
     *                    주소 발급은 저장소를 아는 표현 계층에서 하고 여기서는 받기만 한다.
     */
    public static NoticeDetailResponse from(
            NoticeDetailView view,
            List<NoticeAttachmentResponse> attachments
    ) {
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
                attachments == null ? List.of() : attachments,
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

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

        @io.swagger.v3.oas.annotations.media.Schema(
                description = """
                        이 공지로 AI 일정 등록을 이미 했는지 여부.
                        true 이면 AI 일정 등록 컴포넌트를 그리지 않는다. 1회성이라 되돌아가지 않는다.
                        운영진에게만 뜨는 화면이므로 훈련생 화면에서는 쓸 일이 없다.
                        """,
                example = "false"
        )
        boolean aiCalendarRegistered,

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
                view.calendarRegistered(),
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

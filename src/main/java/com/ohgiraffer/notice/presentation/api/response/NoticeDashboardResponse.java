package com.ohgiraffer.notice.presentation.api.response;

import com.ohgiraffer.notice.application.query.NoticeDashboardView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 메인 대시보드 공지 카드. 시각은 서버에서 한국 시간으로 변환해 내려준다.
 */
@Schema(description = "메인 대시보드 공지 카드")
public record NoticeDashboardResponse(

        @Schema(description = "공지 식별자", example = "1")
        Long noticeId,

        @Schema(description = "공지 제목", example = "8월 휴강일 공지")
        String title,

        @Schema(description = "목록 상단 고정 여부", example = "true")
        boolean pinned,

        @Schema(description = "등록일시 (KST)", example = "2026-08-05T14:20:00")
        LocalDateTime createdAt
) {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static NoticeDashboardResponse from(NoticeDashboardView view) {
        return new NoticeDashboardResponse(
                view.noticeId(),
                view.title(),
                view.pinned(),
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

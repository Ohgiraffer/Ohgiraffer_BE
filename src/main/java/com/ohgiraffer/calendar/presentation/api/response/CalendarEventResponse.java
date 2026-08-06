package com.ohgiraffer.calendar.presentation.api.response;

import com.ohgiraffer.calendar.application.query.CalendarEventView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 월간 캘린더의 일정 하나. 시각은 서버에서 한국 시간으로 변환해 내려준다.
 *
 * <p>{@code editable} 은 화면이 삭제 체크박스를 활성화할지 판단하는 데 쓴다.
 * 요구사항상 일정은 등록자만 지울 수 있다.
 */
@Schema(description = "캘린더 일정")
public record CalendarEventResponse(

        @Schema(description = "일정 식별자", example = "1")
        Long calendarEventId,

        @Schema(description = "일정명", example = "알고리즘 특강")
        String title,

        @Schema(
                description = "일정 유형. 화면은 이 값으로 색을 정한다",
                example = "CLASS"
        )
        String eventType,

        @Schema(description = "시작일시 (KST)", example = "2026-08-10T10:00:00")
        LocalDateTime startTime,

        @Schema(description = "종료일시 (KST)", example = "2026-08-10T12:00:00")
        LocalDateTime endTime,

        @Schema(description = "종일 일정 여부", example = "false")
        boolean allDay,

        @Schema(description = "장소", example = "623호")
        String location,

        @Schema(description = "내가 등록한 일정인지 여부. 삭제 가능 여부와 같다", example = "true")
        boolean editable
) {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static CalendarEventResponse from(
            CalendarEventView view,
            Long viewerId
    ) {
        return new CalendarEventResponse(
                view.calendarEventId(),
                view.title(),
                view.eventType(),
                toKst(view.startTime()),
                toKst(view.endTime()),
                view.allDay(),
                view.location(),
                view.createdBy() != null && view.createdBy().equals(viewerId)
        );
    }

    private static LocalDateTime toKst(Instant instant) {
        if (instant == null) {
            return null;
        }

        return instant.atZone(KST).toLocalDateTime();
    }
}

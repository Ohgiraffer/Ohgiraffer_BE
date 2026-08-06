package com.ohgiraffer.calendar.presentation.api.response;

import com.ohgiraffer.calendar.domain.model.CalendarEvent;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 캘린더 일정 등록 응답. 시각은 서버에서 한국 시간으로 변환해 내려준다.
 */
@Schema(description = "캘린더 일정 등록 결과")
public record CreateCalendarEventResponse(

        @Schema(description = "일정 식별자", example = "1")
        Long calendarEventId,

        @Schema(description = "일정명", example = "알고리즘 특강")
        String title,

        @Schema(description = "일정 유형", example = "CLASS")
        String eventType,

        @Schema(description = "시작일시 (KST)", example = "2026-08-10T10:00:00")
        LocalDateTime startTime,

        @Schema(description = "종료일시 (KST)", example = "2026-08-10T12:00:00")
        LocalDateTime endTime,

        @Schema(description = "종일 일정 여부", example = "false")
        boolean allDay,

        @Schema(description = "장소", example = "623호")
        String location
) {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static CreateCalendarEventResponse from(CalendarEvent event) {
        return new CreateCalendarEventResponse(
                event.getId(),
                event.getTitle(),
                event.getEventType().name(),
                toKst(event.getStartTime()),
                toKst(event.getEndTime()),
                event.isAllDay(),
                event.getLocation()
        );
    }

    private static LocalDateTime toKst(Instant instant) {
        if (instant == null) {
            return null;
        }

        return instant.atZone(KST).toLocalDateTime();
    }
}

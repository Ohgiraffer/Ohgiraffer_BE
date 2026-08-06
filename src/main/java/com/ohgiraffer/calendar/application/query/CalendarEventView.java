package com.ohgiraffer.calendar.application.query;

import com.ohgiraffer.calendar.domain.model.CalendarEvent;

import java.time.Instant;

/**
 * 월간 캘린더 칸에 그릴 일정 하나.
 *
 * <p>{@code eventType} 으로 화면이 색을 정한다. 어떤 유형을 무슨 색으로 묶을지는
 * 화면의 판단이므로 서버는 유형만 내려준다.
 */
public record CalendarEventView(
        Long calendarEventId,
        String title,
        String eventType,
        Instant startTime,
        Instant endTime,
        boolean allDay,
        String location,
        Long createdBy
) {

    public static CalendarEventView of(CalendarEvent event) {
        return new CalendarEventView(
                event.getId(),
                event.getTitle(),
                event.getEventType().name(),
                event.getStartTime(),
                event.getEndTime(),
                event.isAllDay(),
                event.getLocation(),
                event.getCreatedBy()
        );
    }
}

package com.ohgiraffer.notice.application.command;

import com.ohgiraffer.calendar.domain.model.EventType;

import java.time.Instant;

/**
 * 캘린더에 등록할 일정 하나.
 *
 * <p>AI 가 뽑은 값 그대로가 아니라 운영진이 모달에서 확인하고 고친 값이다.
 * 그래서 유형이 반드시 채워져 있다. 화면에서 유형을 고르지 않은 후보는 아예 넘어오지 않는다.
 *
 * <p>시각은 이미 {@code Instant} 로 합쳐진 상태로 들어온다. 캘린더 등록과 같은 방식이다.
 */
public record RegisterNoticeScheduleCommand(
        String title,
        EventType eventType,
        Instant startTime,
        Instant endTime,
        boolean allDay,
        String location
) {
}

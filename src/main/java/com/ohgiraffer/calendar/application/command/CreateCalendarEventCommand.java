package com.ohgiraffer.calendar.application.command;

import com.ohgiraffer.calendar.domain.model.EventType;

import java.time.Instant;

/**
 * 캘린더 일정 등록 명령.
 *
 * <p>시각은 이미 Instant 로 합쳐진 상태로 들어온다. 화면이 날짜와 시각을 따로 받고
 * 시각은 선택 입력이라, 둘을 합치고 종일 여부를 판단하는 일은 표현 계층이 맡는다.
 *
 * @param eventType 훈련생이 등록하면 표현 계층에서 개인 일정으로 채워 넘어온다
 */
public record CreateCalendarEventCommand(
        String title,
        EventType eventType,
        Instant startTime,
        Instant endTime,
        boolean allDay,
        String location,
        Long createdBy
) {
}

package com.ohgiraffer.calendar.domain.repository;

import com.ohgiraffer.calendar.domain.model.CalendarEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 캘린더 일정 영속성 포트. 구현은 infrastructure 계층의 어댑터가 담당한다.
 */
public interface CalendarEventRepository {

    CalendarEvent save(CalendarEvent calendarEvent);

    /**
     * 주어진 기간에 걸쳐 있고, 해당 사용자에게 보여줄 수 있는 일정을 시작순으로 반환한다.
     *
     * <p>"기간 안에 시작하는" 이 아니라 "기간에 걸치는" 이다. 여러 날짜에 걸친 일정은
     * 지난달에 시작했더라도 이번 달 칸에 그려져야 하기 때문이다.
     *
     * <p>개인 일정을 거르는 일도 여기서 함께 한다. 전부 읽어와 애플리케이션에서 버리면
     * 남의 개인 일정이 잠시라도 메모리에 올라오고, 일정이 쌓일수록 버리는 양만 늘어난다.
     */
    List<CalendarEvent> findVisibleInPeriod(
            Instant from,
            Instant to,
            Long userId
    );

    Optional<CalendarEvent> findById(Long calendarEventId);

    void deleteById(Long calendarEventId);
}

package com.ohgiraffer.aiassistant.application.port;

import com.ohgiraffer.calendar.application.query.CalendarEventView;

import java.util.List;

public interface CalendarQueryPort {

    // 오늘 날짜에 걸치는 일정만 반환
    List<CalendarEventView> getTodayEvents(Long userId);

}

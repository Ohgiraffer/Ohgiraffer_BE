package com.ohgiraffer.calendar.application.usecase;

import com.ohgiraffer.calendar.application.query.CalendarEventView;

import java.util.List;

/**
 * 캘린더 일정 조회 유스케이스.
 *
 * <p>개인 일정은 보는 사람마다 다르므로 조회할 때 사용자 식별자를 함께 받는다.
 */
public interface CalendarEventQueryUseCase {

    /**
     * 해당 월에 걸치는 일정을 시작순으로 반환한다.
     *
     * <p>지난달에 시작해 이번 달까지 이어지는 일정도 포함한다.
     */
    List<CalendarEventView> findByMonth(int year, int month, Long userId);
}

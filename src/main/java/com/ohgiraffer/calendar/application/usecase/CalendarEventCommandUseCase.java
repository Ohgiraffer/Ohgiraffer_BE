package com.ohgiraffer.calendar.application.usecase;

import com.ohgiraffer.calendar.application.command.CreateCalendarEventCommand;
import com.ohgiraffer.calendar.domain.model.CalendarEvent;

/**
 * 캘린더 일정 쓰기 유스케이스. 다른 도메인이 일정을 다뤄야 할 때도 이 인터페이스만 호출한다.
 */
public interface CalendarEventCommandUseCase {

    CalendarEvent create(CreateCalendarEventCommand command);

    /**
     * 일정을 삭제한다. 요구사항상 등록자 본인만 지울 수 있다.
     *
     * <p>볼 수 없는 일정은 존재를 알리지 않는다. 남의 개인 일정에 삭제를 시도하면
     * 403 이 아니라 404 로 답한다.
     */
    void delete(Long calendarEventId, Long requesterId);
}

package com.ohgiraffer.calendar.application.service;

import com.ohgiraffer.calendar.application.command.CreateCalendarEventCommand;
import com.ohgiraffer.calendar.application.usecase.CalendarEventCommandUseCase;
import com.ohgiraffer.calendar.domain.model.CalendarEvent;
import com.ohgiraffer.calendar.domain.repository.CalendarEventRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CalendarEventCommandService implements CalendarEventCommandUseCase {

    private final CalendarEventRepository calendarEventRepository;

    public CalendarEventCommandService(
            CalendarEventRepository calendarEventRepository
    ) {
        this.calendarEventRepository = calendarEventRepository;
    }

    @Override
    public CalendarEvent create(CreateCalendarEventCommand command) {
        CalendarEvent calendarEvent = CalendarEvent.create(
                command.title(),
                command.eventType(),
                command.startTime(),
                command.endTime(),
                command.allDay(),
                command.location(),
                command.createdBy()
        );

        return calendarEventRepository.save(calendarEvent);
    }

    @Override
    public void delete(Long calendarEventId, Long requesterId) {
        CalendarEvent calendarEvent = calendarEventRepository
                .findById(calendarEventId)
                .orElseThrow(CalendarEventCommandService::eventNotFound);

        /*
         * 볼 수 없는 일정은 없는 것처럼 답한다. 403 을 주면 그 번호에 남의 개인 일정이
         * 존재한다는 사실이 드러난다. 조회에서 감춘 것을 삭제 경로가 흘리면 의미가 없다.
         */
        if (!calendarEvent.isVisibleTo(requesterId)) {
            throw eventNotFound();
        }

        /*
         * 볼 수 있는 일정이라도 지우는 것은 등록자만 가능하다.
         * 여기서는 403 이 맞다 — 화면에 이미 보이는 일정이라 존재를 숨길 이유가 없고,
         * "남의 일정이라 지울 수 없다" 를 알려주는 편이 낫다.
         */
        if (!calendarEvent.isCreatedBy(requesterId)) {
            throw new BusinessException(ErrorCode.CALENDAR_EVENT_NOT_CREATOR);
        }

        calendarEventRepository.deleteById(calendarEventId);
    }

    private static BusinessException eventNotFound() {
        return new BusinessException(ErrorCode.CALENDAR_EVENT_NOT_FOUND);
    }
}

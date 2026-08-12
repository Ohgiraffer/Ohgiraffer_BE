package com.ohgiraffer.notice.infrastructure.adapter;

import com.ohgiraffer.calendar.domain.model.CalendarEvent;
import com.ohgiraffer.calendar.domain.repository.CalendarEventRepository;
import com.ohgiraffer.notice.application.command.RegisterNoticeScheduleCommand;
import com.ohgiraffer.notice.application.port.CalendarEventRegistrationPort;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 확정된 일정을 캘린더에 저장한다.
 *
 * <p>{@code createFromAiExtraction} 으로 만들어 {@code aiExtracted} 를 켠다. 사람이 직접
 * 넣은 일정과 구분되어야 화면이 다르게 표시할 수 있다.
 *
 * <p>공지 식별자를 함께 남기지 않는다. 요구사항이 "공지를 수정하거나 삭제해도 캘린더에
 * 등록된 일정에는 영향이 없다" 라, 연결을 남겨도 따라 지울 일이 없다.
 */
@Component
public class CalendarEventRegistrationAdapter
        implements CalendarEventRegistrationPort {

    private final CalendarEventRepository calendarEventRepository;

    public CalendarEventRegistrationAdapter(
            CalendarEventRepository calendarEventRepository
    ) {
        this.calendarEventRepository = calendarEventRepository;
    }

    @Override
    public int register(
            List<RegisterNoticeScheduleCommand> schedules,
            Long createdBy
    ) {
        for (RegisterNoticeScheduleCommand schedule : schedules) {
            calendarEventRepository.save(CalendarEvent.createFromAiExtraction(
                    schedule.title(),
                    schedule.eventType(),
                    schedule.startTime(),
                    schedule.endTime(),
                    schedule.allDay(),
                    schedule.location(),
                    createdBy
            ));
        }

        return schedules.size();
    }
}

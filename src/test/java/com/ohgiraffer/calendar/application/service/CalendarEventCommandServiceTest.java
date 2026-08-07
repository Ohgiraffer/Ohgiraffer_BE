package com.ohgiraffer.calendar.application.service;

import com.ohgiraffer.calendar.application.command.CreateCalendarEventCommand;
import com.ohgiraffer.calendar.domain.model.CalendarEvent;
import com.ohgiraffer.calendar.domain.model.EventType;
import com.ohgiraffer.calendar.domain.repository.CalendarEventRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarEventCommandServiceTest {

    private static final Long EVENT_ID = 10L;
    private static final Long OWNER_ID = 7L;
    private static final Long OTHER_USER_ID = 99L;
    private static final Instant START =
            Instant.parse("2026-08-10T01:00:00Z");
    private static final Instant END =
            Instant.parse("2026-08-10T03:00:00Z");

    @Mock
    private CalendarEventRepository calendarEventRepository;

    private CalendarEventCommandService calendarEventCommandService;

    @BeforeEach
    void setUp() {
        calendarEventCommandService =
                new CalendarEventCommandService(calendarEventRepository);
    }

    @Test
    @DisplayName("일정을 등록하면 저장소에 전달하고 저장된 일정을 돌려준다")
    void createEvent() {
        when(calendarEventRepository.save(any(CalendarEvent.class)))
                .thenAnswer(invocation -> saved(invocation.getArgument(0)));

        CalendarEvent result = calendarEventCommandService.create(
                new CreateCalendarEventCommand(
                        "알고리즘 특강",
                        EventType.CLASS,
                        START,
                        END,
                        false,
                        "623호",
                        OWNER_ID
                )
        );

        ArgumentCaptor<CalendarEvent> captor =
                ArgumentCaptor.forClass(CalendarEvent.class);
        verify(calendarEventRepository).save(captor.capture());

        assertEquals("알고리즘 특강", captor.getValue().getTitle());
        assertEquals(EventType.CLASS, captor.getValue().getEventType());
        assertEquals(OWNER_ID, captor.getValue().getCreatedBy());
        assertEquals(EVENT_ID, result.getId());
    }

    @Test
    @DisplayName("업무 규칙을 어기면 저장소를 호출하지 않는다")
    void createDoesNotTouchRepositoryWhenDomainRuleFails() {
        assertThrows(
                BusinessException.class,
                () -> calendarEventCommandService.create(
                        new CreateCalendarEventCommand(
                                "   ",
                                EventType.CLASS,
                                START,
                                END,
                                false,
                                null,
                                OWNER_ID
                        ))
        );

        verify(calendarEventRepository, never()).save(any(CalendarEvent.class));
    }

    @Test
    @DisplayName("등록자 본인이면 일정을 삭제할 수 있다")
    void deleteEvent() {
        when(calendarEventRepository.findById(EVENT_ID))
                .thenReturn(Optional.of(stored(EventType.CLASS, OWNER_ID)));

        calendarEventCommandService.delete(EVENT_ID, OWNER_ID);

        verify(calendarEventRepository).deleteById(EVENT_ID);
    }

    @Test
    @DisplayName("등록자가 아니면 삭제할 수 없고 저장소를 건드리지 않는다")
    void deleteFailsWhenNotCreator() {
        when(calendarEventRepository.findById(EVENT_ID))
                .thenReturn(Optional.of(stored(EventType.CLASS, OWNER_ID)));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> calendarEventCommandService.delete(EVENT_ID, OTHER_USER_ID)
        );

        assertEquals(
                ErrorCode.CALENDAR_EVENT_NOT_CREATOR,
                exception.getErrorCode()
        );
        verify(calendarEventRepository, never()).deleteById(EVENT_ID);
    }

    @Test
    @DisplayName("볼 수 없는 개인 일정은 존재 자체를 알리지 않는다")
    void deleteHidesInvisiblePersonalEvent() {
        when(calendarEventRepository.findById(EVENT_ID))
                .thenReturn(Optional.of(stored(EventType.PERSONAL, OWNER_ID)));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> calendarEventCommandService.delete(EVENT_ID, OTHER_USER_ID)
        );

        /*
         * 403이면 "그 번호에 남의 개인 일정이 있다"는 사실이 드러나므로 404여야 한다.
         * 조회에서 감춘 것을 삭제 경로가 흘리면 감춘 의미가 없다.
         */
        assertEquals(
                ErrorCode.CALENDAR_EVENT_NOT_FOUND,
                exception.getErrorCode()
        );
        verify(calendarEventRepository, never()).deleteById(EVENT_ID);
    }

    @Test
    @DisplayName("본인 개인 일정은 삭제할 수 있다")
    void deleteOwnPersonalEvent() {
        when(calendarEventRepository.findById(EVENT_ID))
                .thenReturn(Optional.of(stored(EventType.PERSONAL, OWNER_ID)));

        calendarEventCommandService.delete(EVENT_ID, OWNER_ID);

        verify(calendarEventRepository).deleteById(EVENT_ID);
    }

    @Test
    @DisplayName("없는 일정은 삭제할 수 없다")
    void deleteFailsWhenEventMissing() {
        when(calendarEventRepository.findById(EVENT_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> calendarEventCommandService.delete(EVENT_ID, OWNER_ID)
        );

        assertEquals(
                ErrorCode.CALENDAR_EVENT_NOT_FOUND,
                exception.getErrorCode()
        );
        verify(calendarEventRepository, never()).deleteById(EVENT_ID);
    }

    private CalendarEvent stored(EventType type, Long createdBy) {
        return CalendarEvent.restore(
                EVENT_ID,
                "알고리즘 특강",
                type,
                START,
                END,
                false,
                "623호",
                createdBy,
                false,
                false
        );
    }

    private CalendarEvent saved(CalendarEvent event) {
        return CalendarEvent.restore(
                EVENT_ID,
                event.getTitle(),
                event.getEventType(),
                event.getStartTime(),
                event.getEndTime(),
                event.isAllDay(),
                event.getLocation(),
                event.getCreatedBy(),
                false,
                false
        );
    }
}

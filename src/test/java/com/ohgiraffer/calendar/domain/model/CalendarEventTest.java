package com.ohgiraffer.calendar.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalendarEventTest {

    private static final Long OWNER_ID = 7L;
    private static final Long OTHER_USER_ID = 99L;
    private static final Instant START =
            Instant.parse("2026-08-10T01:00:00Z");
    private static final Instant END =
            Instant.parse("2026-08-10T03:00:00Z");

    @Test
    @DisplayName("일정을 등록하면 입력한 값을 그대로 담는다")
    void createEvent() {
        CalendarEvent event = event(EventType.CLASS, START, END);

        assertEquals("알고리즘 특강", event.getTitle());
        assertEquals(EventType.CLASS, event.getEventType());
        assertEquals(START, event.getStartTime());
        assertEquals(END, event.getEndTime());
        assertEquals(OWNER_ID, event.getCreatedBy());
    }

    @Test
    @DisplayName("일정명 앞뒤 공백은 떼고 담는다")
    void trimsTitle() {
        CalendarEvent event = CalendarEvent.create(
                "  알고리즘 특강  ",
                EventType.CLASS,
                START,
                END,
                false,
                "  623호  ",
                OWNER_ID
        );

        assertEquals("알고리즘 특강", event.getTitle());
        assertEquals("623호", event.getLocation());
    }

    @Test
    @DisplayName("장소를 비우면 null 로 담는다")
    void blankLocationBecomesNull() {
        CalendarEvent event = CalendarEvent.create(
                "알고리즘 특강",
                EventType.CLASS,
                START,
                END,
                false,
                "   ",
                OWNER_ID
        );

        assertEquals(null, event.getLocation());
    }

    @Test
    @DisplayName("일정명이 비면 등록할 수 없다")
    void rejectsBlankTitle() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> CalendarEvent.create(
                        "  ", EventType.CLASS, START, END, false, null, OWNER_ID)
        );

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
    }

    @Test
    @DisplayName("종료가 시작보다 앞서면 등록할 수 없다")
    void rejectsReversedPeriod() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> event(EventType.CLASS, END, START)
        );

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
    }

    @Test
    @DisplayName("시작과 종료가 같은 시각이면 등록할 수 있다")
    void allowsSameInstant() {
        CalendarEvent event = event(EventType.CLASS, START, START);

        assertEquals(START, event.getEndTime());
    }

    @Test
    @DisplayName("개인 일정은 등록한 본인에게만 보인다")
    void personalEventIsVisibleOnlyToOwner() {
        CalendarEvent event = event(EventType.PERSONAL, START, END);

        assertTrue(event.isVisibleTo(OWNER_ID));
        assertFalse(event.isVisibleTo(OTHER_USER_ID));
    }

    @Test
    @DisplayName("공용 일정은 등록자가 아니어도 보인다")
    void sharedEventIsVisibleToEveryone() {
        for (EventType type : EventType.values()) {
            /*
             * 개인 일정은 대상이 아니고, 공휴일은 create() 로 만들 수 없다.
             * 공휴일의 가시성은 별도 테스트에서 restore() 로 확인한다.
             */
            if (type.isPersonal() || type.isSystemOnly()) {
                continue;
            }

            assertTrue(event(type, START, END).isVisibleTo(OTHER_USER_ID));
        }
    }

    @Test
    @DisplayName("등록자만 자기 일정으로 인정한다")
    void identifiesCreator() {
        CalendarEvent event = event(EventType.CLASS, START, END);

        assertTrue(event.isCreatedBy(OWNER_ID));
        assertFalse(event.isCreatedBy(OTHER_USER_ID));
    }

    @Test
    @DisplayName("직접 등록한 일정은 자동 등록도 AI 추출도 아니다")
    void manualEventCarriesNoAutomationFlag() {
        CalendarEvent event = event(EventType.CLASS, START, END);

        assertFalse(event.isAutoRegistered());
        assertFalse(event.isAiExtracted());
    }

    @Test
    @DisplayName("공휴일은 사람이 직접 등록할 수 없다")
    void rejectsHolidayFromUser() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> event(EventType.HOLIDAY, START, END)
        );

        /*
         * 사람이 만들 수 있게 두면 등록자가 있는 가짜 공휴일이 생기고,
         * 화면에서는 진짜와 구분되지 않는다.
         */
        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
    }

    @Test
    @DisplayName("등록자가 없는 공휴일은 아무도 삭제할 수 없다")
    void holidayHasNoOwner() {
        CalendarEvent holiday = CalendarEvent.restore(
                1L,
                "광복절",
                EventType.HOLIDAY,
                START,
                END,
                true,
                null,
                null,
                true,
                false
        );

        assertFalse(holiday.isCreatedBy(OWNER_ID));
        assertFalse(holiday.isCreatedBy(OTHER_USER_ID));
        assertFalse(holiday.isCreatedBy(null));

        /*
         * 삭제는 막되 조회는 모두에게 열려 있어야 한다.
         */
        assertTrue(holiday.isVisibleTo(OWNER_ID));
        assertTrue(holiday.isVisibleTo(OTHER_USER_ID));
    }

    private CalendarEvent event(EventType type, Instant start, Instant end) {
        return CalendarEvent.create(
                "알고리즘 특강", type, start, end, false, "623호", OWNER_ID);
    }
}

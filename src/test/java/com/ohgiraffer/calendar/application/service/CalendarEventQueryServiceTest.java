package com.ohgiraffer.calendar.application.service;

import com.ohgiraffer.calendar.application.query.CalendarEventView;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarEventQueryServiceTest {

    private static final Long VIEWER_ID = 7L;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Mock
    private CalendarEventRepository calendarEventRepository;

    private CalendarEventQueryService calendarEventQueryService;

    @BeforeEach
    void setUp() {
        calendarEventQueryService =
                new CalendarEventQueryService(calendarEventRepository);
    }

    @Test
    @DisplayName("조회 기간은 한국 시간 기준 그달 1일 0시부터 말일 끝까지다")
    void monthRangeUsesKstBoundaries() {
        when(calendarEventRepository.findVisibleInPeriod(any(), any(), any()))
                .thenReturn(List.of());

        calendarEventQueryService.findByMonth(2026, 8, VIEWER_ID);

        ArgumentCaptor<Instant> from = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> to = ArgumentCaptor.forClass(Instant.class);
        verify(calendarEventRepository)
                .findVisibleInPeriod(from.capture(), to.capture(), eq(VIEWER_ID));

        /*
         * UTC 로 자르면 한국 기준 월초와 월말 하루가 어긋난다.
         */
        assertEquals(
                LocalDate.of(2026, 8, 1).atStartOfDay(KST).toInstant(),
                from.getValue()
        );
        assertEquals(
                LocalDate.of(2026, 8, 31)
                        .atTime(23, 59, 59, 999_999_000)
                        .atZone(KST)
                        .toInstant(),
                to.getValue()
        );
    }

    @Test
    @DisplayName("말일이 다른 달도 정확히 계산한다")
    void handlesMonthsOfDifferentLength() {
        when(calendarEventRepository.findVisibleInPeriod(any(), any(), any()))
                .thenReturn(List.of());

        calendarEventQueryService.findByMonth(2028, 2, VIEWER_ID);

        ArgumentCaptor<Instant> to = ArgumentCaptor.forClass(Instant.class);
        verify(calendarEventRepository)
                .findVisibleInPeriod(any(), to.capture(), any());

        /*
         * 2028년은 윤년이라 2월 말일이 29일이다.
         */
        assertEquals(
                LocalDate.of(2028, 2, 29)
                        .atTime(23, 59, 59, 999_999_000)
                        .atZone(KST)
                        .toInstant(),
                to.getValue()
        );
    }

    @Test
    @DisplayName("저장소가 돌려준 일정을 그대로 담아 반환한다")
    void mapsEventsToViews() {
        when(calendarEventRepository.findVisibleInPeriod(any(), any(), any()))
                .thenReturn(List.of(event(EventType.CLASS)));

        List<CalendarEventView> views =
                calendarEventQueryService.findByMonth(2026, 8, VIEWER_ID);

        assertEquals(1, views.size());
        assertEquals("알고리즘 특강", views.get(0).title());
        assertEquals("CLASS", views.get(0).eventType());
        assertEquals(VIEWER_ID, views.get(0).createdBy());
    }

    @Test
    @DisplayName("일정이 없으면 빈 목록을 돌려준다")
    void returnsEmptyList() {
        when(calendarEventRepository.findVisibleInPeriod(any(), any(), any()))
                .thenReturn(List.of());

        assertTrue(calendarEventQueryService
                .findByMonth(2026, 8, VIEWER_ID)
                .isEmpty());
    }

    @Test
    @DisplayName("월이 1~12 를 벗어나면 저장소를 부르지 않고 막는다")
    void rejectsInvalidMonth() {
        for (int month : new int[] {0, 13, -1}) {
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> calendarEventQueryService
                            .findByMonth(2026, month, VIEWER_ID)
            );

            assertEquals(
                    ErrorCode.INVALID_INPUT_VALUE,
                    exception.getErrorCode()
            );
        }

        verify(calendarEventRepository, never())
                .findVisibleInPeriod(any(), any(), any());
    }

    @Test
    @DisplayName("연도가 상식적인 범위를 벗어나면 막는다")
    void rejectsInvalidYear() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> calendarEventQueryService.findByMonth(1000, 8, VIEWER_ID)
        );

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
        verify(calendarEventRepository, never())
                .findVisibleInPeriod(any(), any(), any());
    }

    private CalendarEvent event(EventType type) {
        return CalendarEvent.restore(
                1L,
                "알고리즘 특강",
                type,
                LocalDate.of(2026, 8, 10).atTime(10, 0).atZone(KST).toInstant(),
                LocalDate.of(2026, 8, 10).atTime(12, 0).atZone(KST).toInstant(),
                false,
                "623호",
                VIEWER_ID,
                false,
                false
        );
    }
}

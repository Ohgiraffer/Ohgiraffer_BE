package com.ohgiraffer.calendar.presentation.api;

import com.ohgiraffer.calendar.application.command.CreateCalendarEventCommand;
import com.ohgiraffer.calendar.application.query.CalendarEventView;
import com.ohgiraffer.calendar.application.usecase.CalendarEventCommandUseCase;
import com.ohgiraffer.calendar.application.usecase.CalendarEventQueryUseCase;
import com.ohgiraffer.calendar.domain.model.CalendarEvent;
import com.ohgiraffer.calendar.domain.model.EventType;
import com.ohgiraffer.calendar.presentation.api.request.CreateCalendarEventRequest;
import com.ohgiraffer.calendar.presentation.api.response.CalendarEventResponse;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 역할에 따라 일정 유형이 정해지는지, 날짜와 시각이 올바르게 합쳐지는지 확인한다.
 */
@ExtendWith(MockitoExtension.class)
class CalendarEventControllerTest {

    private static final Long LOGIN_USER_ID = 7L;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Mock
    private CalendarEventCommandUseCase calendarEventCommandUseCase;

    @Mock
    private CalendarEventQueryUseCase calendarEventQueryUseCase;

    private CalendarEventController calendarEventController;

    @BeforeEach
    void setUp() {
        calendarEventController = new CalendarEventController(
                calendarEventCommandUseCase,
                calendarEventQueryUseCase
        );
    }

    @Test
    @DisplayName("훈련생이 수업 유형을 보내도 개인 일정으로 저장한다")
    void traineeAlwaysCreatesPersonalEvent() {
        stubCreate();

        calendarEventController.create(
                principal(Role.STUDENT),
                request("CLASS", LocalTime.of(10, 0), LocalTime.of(12, 0))
        );

        /*
         * 화면에 유형 선택이 없더라도 API 는 직접 호출할 수 있으므로 서버가 정한다.
         */
        assertEquals(EventType.PERSONAL, captured().eventType());
    }

    @Test
    @DisplayName("운영진이 고른 유형은 그대로 저장한다")
    void staffKeepsChosenEventType() {
        stubCreate();

        calendarEventController.create(
                principal(Role.INSTRUCTOR),
                request("CLASS", LocalTime.of(10, 0), LocalTime.of(12, 0))
        );

        assertEquals(EventType.CLASS, captured().eventType());
    }

    @Test
    @DisplayName("운영진이 개인 일정 유형을 보내면 거절한다")
    void staffCannotCreatePersonalEvent() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> calendarEventController.create(
                        principal(Role.INSTRUCTOR),
                        request("PERSONAL", null, null)
                )
        );

        /*
         * 운영진 화면의 유형 목록에 개인 일정이 없다. 조용히 개인 일정으로 바꿔 저장하면
         * 요청과 다른 결과를 돌려주면서 아무 신호도 주지 않는다.
         */
        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
        verify(calendarEventCommandUseCase, never()).create(any());
    }

    @Test
    @DisplayName("등록자는 요청 값이 아니라 로그인 사용자로 채운다")
    void createdByComesFromPrincipal() {
        stubCreate();

        calendarEventController.create(
                principal(Role.MANAGER),
                request("EVENT", null, null)
        );

        assertEquals(LOGIN_USER_ID, captured().createdBy());
    }

    @Test
    @DisplayName("시각을 모두 비우면 그날 0시부터 끝까지인 종일 일정이 된다")
    void allDayEventSpansWholeDay() {
        stubCreate();

        calendarEventController.create(
                principal(Role.INSTRUCTOR),
                request("EVENT", null, null)
        );

        CreateCalendarEventCommand command = captured();

        assertTrue(command.allDay());
        assertEquals(kst(2026, 8, 10, 0, 0), command.startTime());
        assertEquals(
                LocalDate.of(2026, 8, 10)
                        .atTime(23, 59, 59, 999_999_000)
                        .atZone(KST)
                        .toInstant(),
                command.endTime()
        );
    }

    @Test
    @DisplayName("시각을 넣으면 종일 일정이 아니고 그 시각으로 저장한다")
    void timedEventKeepsGivenTimes() {
        stubCreate();

        calendarEventController.create(
                principal(Role.INSTRUCTOR),
                request("CLASS", LocalTime.of(10, 0), LocalTime.of(12, 0))
        );

        CreateCalendarEventCommand command = captured();

        assertFalse(command.allDay());
        assertEquals(kst(2026, 8, 10, 10, 0), command.startTime());
        assertEquals(kst(2026, 8, 10, 12, 0), command.endTime());
    }

    @Test
    @DisplayName("시작 시각만 넣으면 종료는 그날 끝으로 채운다")
    void missingEndTimeFallsBackToEndOfDay() {
        stubCreate();

        calendarEventController.create(
                principal(Role.INSTRUCTOR),
                request("CLASS", LocalTime.of(14, 0), null)
        );

        CreateCalendarEventCommand command = captured();

        /*
         * 한쪽만 비운 것은 종일이 아니다. 오후부터 그날 끝까지로 본다.
         */
        assertFalse(command.allDay());
        assertEquals(kst(2026, 8, 10, 14, 0), command.startTime());
    }

    @Test
    @DisplayName("월간 조회는 연월과 로그인 사용자를 그대로 넘긴다")
    void findByMonthPassesUser() {
        when(calendarEventQueryUseCase.findByMonth(2026, 8, LOGIN_USER_ID))
                .thenReturn(List.of(view(LOGIN_USER_ID)));

        ResponseEntity<List<CalendarEventResponse>> response =
                calendarEventController.findByMonth(
                        principal(Role.STUDENT), 2026, 8);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("알고리즘 특강", response.getBody().get(0).title());
        verify(calendarEventQueryUseCase).findByMonth(2026, 8, LOGIN_USER_ID);
    }

    @Test
    @DisplayName("내가 등록한 일정만 editable 로 내려간다")
    void marksOnlyOwnEventsEditable() {
        when(calendarEventQueryUseCase.findByMonth(2026, 8, LOGIN_USER_ID))
                .thenReturn(List.of(view(LOGIN_USER_ID), view(99L)));

        ResponseEntity<List<CalendarEventResponse>> response =
                calendarEventController.findByMonth(
                        principal(Role.STUDENT), 2026, 8);

        /*
         * 화면은 이 값으로 삭제 체크박스를 켤지 정한다. 요구사항상 등록자만 지울 수 있다.
         */
        assertTrue(response.getBody().get(0).editable());
        assertFalse(response.getBody().get(1).editable());
    }

    @Test
    @DisplayName("삭제는 로그인 사용자로 호출하고 204 로 답한다")
    void deletePassesLoginUser() {
        ResponseEntity<Void> response = calendarEventController.delete(
                principal(Role.STUDENT), 10L);

        assertEquals(204, response.getStatusCode().value());
        verify(calendarEventCommandUseCase).delete(10L, LOGIN_USER_ID);
    }

    private CalendarEventView view(Long createdBy) {
        return new CalendarEventView(
                1L,
                "알고리즘 특강",
                "CLASS",
                kst(2026, 8, 10, 10, 0),
                kst(2026, 8, 10, 12, 0),
                false,
                "623호",
                createdBy
        );
    }

    private void stubCreate() {
        when(calendarEventCommandUseCase.create(any()))
                .thenReturn(CalendarEvent.restore(
                        1L,
                        "알고리즘 특강",
                        EventType.CLASS,
                        kst(2026, 8, 10, 10, 0),
                        kst(2026, 8, 10, 12, 0),
                        false,
                        "623호",
                        LOGIN_USER_ID,
                        false,
                        false
                ));
    }

    private CreateCalendarEventCommand captured() {
        ArgumentCaptor<CreateCalendarEventCommand> captor =
                ArgumentCaptor.forClass(CreateCalendarEventCommand.class);
        verify(calendarEventCommandUseCase).create(captor.capture());

        return captor.getValue();
    }

    private static Instant kst(int y, int m, int d, int hour, int minute) {
        return LocalDate.of(y, m, d)
                .atTime(hour, minute)
                .atZone(KST)
                .toInstant();
    }

    private CreateCalendarEventRequest request(
            String eventType,
            LocalTime startTime,
            LocalTime endTime
    ) {
        return new CreateCalendarEventRequest(
                "알고리즘 특강",
                eventType,
                LocalDate.of(2026, 8, 10),
                startTime,
                LocalDate.of(2026, 8, 10),
                endTime,
                "623호",
                false
        );
    }

    private CustomUserPrincipal principal(Role role) {
        return CustomUserPrincipal.from(new User(
                LOGIN_USER_ID,
                "김훈련",
                "010-0000-0000",
                "trainee@campflow.dev",
                role,
                null,
                "ENCODED_PASSWORD",
                false,
                true,
                LocalDate.of(2026, 8, 1),
                null,
                UserStatus.ACTIVE,
                null
        ));
    }
}

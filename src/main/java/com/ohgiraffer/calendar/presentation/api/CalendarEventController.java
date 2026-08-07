package com.ohgiraffer.calendar.presentation.api;

import com.ohgiraffer.calendar.application.usecase.CalendarEventCommandUseCase;
import com.ohgiraffer.calendar.application.usecase.CalendarEventQueryUseCase;
import com.ohgiraffer.calendar.domain.model.CalendarEvent;
import com.ohgiraffer.calendar.domain.model.EventType;
import com.ohgiraffer.calendar.presentation.api.request.CreateCalendarEventRequest;
import com.ohgiraffer.calendar.presentation.api.response.CalendarEventResponse;
import com.ohgiraffer.calendar.presentation.api.response.CreateCalendarEventResponse;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.exception.ErrorResponse;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import com.ohgiraffer.user.domain.model.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Tag(name = "통합 캘린더", description = "메인 화면의 월간 캘린더에서 사용")
@RestController
@RequestMapping("/calendar-events")
public class CalendarEventController {

    private final CalendarEventCommandUseCase calendarEventCommandUseCase;
    private final CalendarEventQueryUseCase calendarEventQueryUseCase;

    public CalendarEventController(
            CalendarEventCommandUseCase calendarEventCommandUseCase,
            CalendarEventQueryUseCase calendarEventQueryUseCase
    ) {
        this.calendarEventCommandUseCase = calendarEventCommandUseCase;
        this.calendarEventQueryUseCase = calendarEventQueryUseCase;
    }

    /**
     * 월간 캘린더 조회. 화면의 연월 이동 화살표가 호출한다.
     */
    @Operation(
            summary = "월간 캘린더 조회",
            description = """
                    해당 월에 걸치는 일정을 시작순으로 반환한다.
                    지난달에 시작해 이번 달까지 이어지는 일정도 포함하므로,
                    여러 날에 걸친 일정을 가로로 그릴 수 있다.

                    개인 일정은 등록한 본인에게만 내려간다. 남의 면담이나 휴가는 보이지 않는다.
                    editable 이 true 인 일정만 삭제할 수 있다.

                    오늘 일정 카드는 이 결과에서 오늘 날짜에 걸친 것만 화면에서 고르면 된다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공. 비어 있을 수 있다"),
            @ApiResponse(
                    responseCode = "400",
                    description = "연월 범위를 벗어남 (COMMON_001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<List<CalendarEventResponse>> findByMonth(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "조회할 연도", example = "2026")
            @RequestParam int year,
            @Parameter(description = "조회할 월 (1~12)", example = "8")
            @RequestParam int month
    ) {
        Long viewerId = currentUserId(principal);

        List<CalendarEventResponse> events =
                calendarEventQueryUseCase.findByMonth(year, month, viewerId)
                        .stream()
                        .map(view -> CalendarEventResponse.from(view, viewerId))
                        .toList();

        return ResponseEntity.ok(events);
    }

    /**
     * 일정 등록. 훈련생은 개인 일정만, 운영진은 공용 일정만 만들 수 있다.
     */
    @Operation(
            summary = "캘린더 일정 등록",
            description = """
                    훈련생이 등록하면 요청의 eventType 과 무관하게 개인 일정(PERSONAL)으로 저장한다.
                    훈련생 화면에는 유형 선택이 없고, 개인 일정은 등록한 본인에게만 보인다.
                    운영진은 CLASS / PRESENTATION / ASSIGNMENT / EVENT 중에서 고른다.

                    시작 시각과 종료 시각을 모두 비우면 종일 일정으로 저장한다.
                    시각을 비운 쪽은 시작이면 그날 0시, 종료면 그날 끝으로 채운다.

                    notifyTrainees 는 받아만 두고 아직 발송하지 않는다. 알림 도메인이 들어오면 연결한다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            필수 값 누락, 형식 오류, 종료가 시작보다 앞섬,
                            또는 운영진이 개인 일정 유형을 보냄 (COMMON_001)
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<CreateCalendarEventResponse> create(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateCalendarEventRequest request
    ) {
        CalendarEvent created = calendarEventCommandUseCase.create(
                request.toCommand(
                        eventTypeFor(principal, request),
                        currentUserId(principal)
                )
        );

        return ResponseEntity
                .created(URI.create("/calendar-events/" + created.getId()))
                .body(CreateCalendarEventResponse.from(created));
    }

    /**
     * 일정 삭제. 요구사항상 등록자 본인만 지울 수 있다.
     */
    @Operation(
            summary = "캘린더 일정 삭제",
            description = """
                    등록자 본인만 삭제할 수 있다. 화면은 조회 응답의 editable 로
                    체크박스를 켤지 정하므로, 정상 흐름에서는 403 이 나오지 않는다.

                    볼 수 없는 일정에 삭제를 시도하면 403 이 아니라 404 로 답한다.
                    403 을 주면 그 번호에 남의 개인 일정이 있다는 사실이 드러나기 때문이다.

                    여러 건을 선택해 지울 때는 이 API 를 건마다 호출한다.
                    한 건이 실패해도 나머지는 지워지므로 화면에서 결과를 건별로 안내할 수 있다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(
                    responseCode = "403",
                    description = "등록자가 아님 (CALENDAR_002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않거나 볼 수 없는 일정 (CALENDAR_001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/{calendarEventId}")
    public ResponseEntity<Void> delete(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "일정 식별자", example = "1")
            @PathVariable Long calendarEventId
    ) {
        calendarEventCommandUseCase.delete(
                calendarEventId,
                currentUserId(principal)
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * 훈련생이 만든 일정은 항상 개인 일정이고, 운영진은 공용 일정만 만들 수 있다.
     *
     * <p>요청 값을 그대로 믿지 않는 이유는 공지 작성자와 같다. 화면에 선택이 없더라도
     * API 는 직접 호출할 수 있으므로, 훈련생이 수업 일정을 만들지 못하게 서버가 정한다.
     */
    private EventType eventTypeFor(
            CustomUserPrincipal principal,
            CreateCalendarEventRequest request
    ) {
        requireAuthenticated(principal);

        /*
         * 훈련생 화면에는 유형 칸이 없어 값이 오지 않는다. 강제가 아니라 기본값을 채우는 것이라
         * EventType.from 을 부르기 전에 정한다.
         */
        if (principal.getRole() == Role.STUDENT) {
            return EventType.PERSONAL;
        }

        EventType eventType = EventType.from(request.eventType());

        /*
         * 운영진 화면의 유형 목록에는 개인 일정이 없다. 훈련생과 달리 운영진은 값을 직접 골라
         * 보내므로, 조용히 바꿔 저장하면 요청과 다른 결과를 돌려주면서 아무 신호도 주지 않는다.
         */
        if (eventType.isPersonal()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "개인 일정은 훈련생만 등록할 수 있습니다."
            );
        }

        return eventType;
    }

    /**
     * SecurityConfig 가 인증을 요구하므로 정상 흐름에서는 null 이 아니다.
     * 설정이 바뀌어 인증 없이 도달했을 때 NullPointerException 대신 401 로 알리기 위한 방어다.
     */
    private Long currentUserId(CustomUserPrincipal principal) {
        requireAuthenticated(principal);

        return principal.getId();
    }

    private void requireAuthenticated(CustomUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}

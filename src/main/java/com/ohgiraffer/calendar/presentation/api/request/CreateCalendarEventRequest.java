package com.ohgiraffer.calendar.presentation.api.request;

import com.ohgiraffer.calendar.application.command.CreateCalendarEventCommand;
import com.ohgiraffer.calendar.domain.model.EventType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * 캘린더 일정 등록 요청. 화면의 등록 모달과 그대로 대응한다.
 *
 * <p>시각은 선택 입력이라 날짜와 따로 받는다. 둘을 합쳐 저장용 시각으로 바꾸고
 * 종일 여부를 판단하는 일은 여기서 한다.
 */
@Schema(description = "캘린더 일정 등록 요청")
public record CreateCalendarEventRequest(

        @Schema(description = "일정명", example = "알고리즘 특강")
        @NotBlank(message = "일정명은 필수입니다.")
        @Size(max = 255, message = "일정명은 255자를 넘을 수 없습니다.")
        String title,

        @Schema(
                description = """
                        일정 유형. 화면 드롭다운과 이렇게 대응한다.
                        수업/발표 -> CLASS, 행사 -> EVENT, 개인 -> PERSONAL
                        훈련생이 등록하면 이 값과 무관하게 PERSONAL 로 저장된다.
                        PERSONAL 은 유형을 고른 사람이 누구든 등록자에게만 보인다.
                        """,
                example = "CLASS",
                allowableValues = {"CLASS", "EVENT", "PERSONAL"}
        )
        String eventType,

        @Schema(description = "시작일", example = "2026-08-10")
        @NotNull(message = "시작일은 필수입니다.")
        LocalDate startDate,

        @Schema(
                description = "시작 시각. 생략하면 그날 0시로 본다",
                example = "10:00"
        )
        LocalTime startTime,

        @Schema(description = "종료일", example = "2026-08-10")
        @NotNull(message = "종료일은 필수입니다.")
        LocalDate endDate,

        @Schema(
                description = "종료 시각. 생략하면 그날 끝으로 본다",
                example = "12:00"
        )
        LocalTime endTime,

        @Schema(description = "장소", example = "623호")
        @Size(max = 255, message = "장소는 255자를 넘을 수 없습니다.")
        String location,

        @Schema(
                description = """
                        등록 시 전체 훈련생에게 알림을 보낼지 여부. 운영진 화면에만 있다.
                        알림 도메인이 아직 없어 지금은 받아만 두고 발송하지 않는다.
                        """,
                example = "false"
        )
        Boolean notifyTrainees
) {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /**
     * 하루의 마지막 순간. 종일 일정의 종료 시각으로 쓴다.
     *
     * <p>0시로 저장하면 "그날에 걸쳐 있는 일정"을 찾는 조회에서 종일 일정이
     * 시작하자마자 끝난 것으로 잡힌다.
     */
    private static final LocalTime END_OF_DAY = LocalTime.of(23, 59, 59, 999_999_000);

    /**
     * @param eventType 조회자 역할에 따라 컨트롤러가 정해 넘긴다
     */
    public CreateCalendarEventCommand toCommand(
            EventType eventType,
            Long createdBy
    ) {
        return new CreateCalendarEventCommand(
                title,
                eventType,
                toInstant(startDate, startTime, LocalTime.MIDNIGHT),
                toInstant(endDate, endTime, END_OF_DAY),
                isAllDay(),
                location,
                createdBy
        );
    }

    /**
     * 시작·종료 시각을 모두 비우면 종일 일정으로 본다.
     * 화면에서 시각 칸을 건드리지 않은 경우다.
     */
    public boolean isAllDay() {
        return startTime == null && endTime == null;
    }

    private static Instant toInstant(
            LocalDate date,
            LocalTime time,
            LocalTime fallback
    ) {
        return date
                .atTime(time != null ? time : fallback)
                .atZone(KST)
                .toInstant();
    }
}

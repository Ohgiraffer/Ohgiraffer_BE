package com.ohgiraffer.notice.presentation.api.request;

import com.ohgiraffer.calendar.domain.model.EventType;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notice.application.command.RegisterNoticeScheduleCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

/**
 * AI 추출 일정 등록 요청. 모달에서 [이 일정 포함] 을 켠 것만 담아 보낸다.
 *
 * <p>추출 응답을 그대로 되돌려 받지 않는다. 사용자가 값을 고칠 수 있고 유형도 여기서 정해지므로,
 * 서버가 기억해 둔 값이 아니라 화면이 보낸 값이 맞다.
 */
@Schema(description = "AI 추출 일정 등록 요청")
public record RegisterNoticeSchedulesRequest(

        @Schema(description = "등록할 일정. 최소 1건")
        @NotEmpty(message = "등록할 일정을 하나 이상 선택해주세요.")
        @Size(max = 20, message = "한 번에 등록할 수 있는 일정은 20건까지입니다.")
        @Valid
        List<ScheduleRequest> schedules
) {

    public List<RegisterNoticeScheduleCommand> toCommands() {
        return schedules.stream()
                .map(ScheduleRequest::toCommand)
                .toList();
    }

    @Schema(description = "등록할 일정 하나")
    public record ScheduleRequest(

            @Schema(description = "일정명", example = "1차 개인 코딩 테스트")
            @NotBlank(message = "일정명은 필수입니다.")
            @Size(max = 255, message = "일정명은 255자를 넘을 수 없습니다.")
            String title,

            @Schema(
                    description = """
                            일정 유형. AI 가 비워 보낸 경우 화면에서 사용자가 골라야 한다.
                            개인 일정과 공휴일은 이 경로로 등록할 수 없다.
                            """,
                    example = "CLASS",
                    allowableValues = {"CLASS", "PRESENTATION", "ASSIGNMENT", "EVENT"}
            )
            @NotBlank(message = "일정 유형을 선택해주세요.")
            String eventType,

            @Schema(description = "시작일", example = "2026-08-05")
            @NotNull(message = "시작일은 필수입니다.")
            LocalDate startDate,

            @Schema(description = "시작 시각. 비우면 종일 일정", example = "10:00")
            LocalTime startTime,

            @Schema(description = "종료일", example = "2026-08-05")
            @NotNull(message = "종료일은 필수입니다.")
            LocalDate endDate,

            @Schema(description = "종료 시각. 비우면 종일 일정", example = "12:00")
            LocalTime endTime,

            @Schema(description = "장소. 선택", example = "강의실 3층")
            @Size(max = 255, message = "장소는 255자를 넘을 수 없습니다.")
            String location
    ) {

        private static final ZoneId KST = ZoneId.of("Asia/Seoul");

        /**
         * 하루의 마지막 순간. 종일 일정의 종료 시각으로 쓴다. 캘린더 등록과 같은 규칙이다.
         */
        private static final LocalTime END_OF_DAY =
                LocalTime.of(23, 59, 59, 999_999_000);

        RegisterNoticeScheduleCommand toCommand() {
            return new RegisterNoticeScheduleCommand(
                    title,
                    toEventType(),
                    toInstant(startDate, startTime, LocalTime.MIDNIGHT),
                    toInstant(endDate, endTime, END_OF_DAY),
                    isAllDay(),
                    location
            );
        }

        /**
         * 개인 일정과 공휴일은 거절한다.
         *
         * <p>개인 일정은 훈련생만 만들 수 있고, 공지 본문의 일정은 애초에 개인 일정일 수 없다.
         * 공휴일은 시스템이 넣는 값이다. 화면 목록에도 없지만, 요청은 화면을 거치지 않고도
         * 올 수 있으므로 서버에서 막는다.
         */
        private EventType toEventType() {
            EventType parsed = EventType.from(eventType);

            if (parsed.isPersonal() || parsed.isSystemOnly()) {
                throw new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "공지에서 등록할 수 없는 일정 유형입니다: " + eventType
                );
            }

            return parsed;
        }

        private boolean isAllDay() {
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
}

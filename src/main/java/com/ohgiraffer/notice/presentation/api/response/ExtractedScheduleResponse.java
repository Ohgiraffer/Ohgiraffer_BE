package com.ohgiraffer.notice.presentation.api.response;

import com.ohgiraffer.notice.domain.model.ExtractedSchedule;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "공지에서 추출한 일정 후보")
public record ExtractedScheduleResponse(

        @Schema(description = "일정명", example = "1차 개인 코딩 테스트")
        String title,

        @Schema(
                description = """
                        일정 유형. AI 가 확신하지 못하면 null 이다.
                        화면에서 사용자가 골라야 등록할 수 있다.
                        """,
                example = "CLASS",
                allowableValues = {"CLASS", "PRESENTATION", "ASSIGNMENT", "EVENT"}
        )
        String eventType,

        @Schema(description = "시작일", example = "2026-08-05")
        LocalDate startDate,

        @Schema(description = "시작 시각. 본문에 없으면 null", example = "10:00")
        LocalTime startTime,

        @Schema(description = "종료일. 하루짜리면 시작일과 같다", example = "2026-08-05")
        LocalDate endDate,

        @Schema(description = "종료 시각. 본문에 없으면 null", example = "12:00")
        LocalTime endTime,

        @Schema(description = "장소. 본문에 없으면 null", example = "강의실 3층")
        String location
) {

    public static ExtractedScheduleResponse from(ExtractedSchedule schedule) {
        return new ExtractedScheduleResponse(
                schedule.title(),
                schedule.eventType() == null ? null : schedule.eventType().name(),
                schedule.startDate(),
                schedule.startTime(),
                schedule.endDate(),
                schedule.endTime(),
                schedule.location()
        );
    }
}

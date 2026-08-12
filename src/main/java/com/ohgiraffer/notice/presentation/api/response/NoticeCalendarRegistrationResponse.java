package com.ohgiraffer.notice.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 추출 일정 등록 결과")
public record NoticeCalendarRegistrationResponse(

        @Schema(description = "캘린더에 등록한 일정 수", example = "2")
        int registeredCount
) {
}

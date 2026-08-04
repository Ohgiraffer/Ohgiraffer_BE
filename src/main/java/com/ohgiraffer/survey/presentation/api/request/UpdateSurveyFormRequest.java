package com.ohgiraffer.survey.presentation.api.request;

import com.ohgiraffer.survey.domain.model.SurveyFormStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateSurveyFormRequest(

        @NotBlank(message = "설문 제목은 필수입니다.")
        @Size(
                max = 255,
                message = "설문 제목은 255자 이하여야 합니다."
        )
        String title,

        @NotNull(message = "응답 마감 일시는 필수입니다.")
        LocalDateTime dueAt,

        @NotNull(message = "설문 상태는 필수입니다.")
        SurveyFormStatus status
) {
}
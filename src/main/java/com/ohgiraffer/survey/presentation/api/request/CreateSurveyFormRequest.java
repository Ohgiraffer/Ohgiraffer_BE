package com.ohgiraffer.survey.presentation.api.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateSurveyFormRequest(

        @NotBlank(message = "설문 제목은 필수입니다.")
        @Size(max = 255, message = "설문 제목은 255자 이하여야 합니다.")
        String title,

        @NotNull(message = "응답 마감일은 필수입니다.")
        @Future(message = "응답 마감일은 현재보다 이후여야 합니다.")
        LocalDateTime dueAt
) {
}
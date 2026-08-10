package com.ohgiraffer.survey.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ohgiraffer.survey.application.usecase.SurveyFormDetailResult;
import com.ohgiraffer.survey.domain.model.SurveyFormStatus;

import java.time.Instant;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SurveyFormDetailResponse(
        Long surveyFormId,
        String title,
        LocalDateTime dueAt,
        SurveyFormStatus status,
        String googleFormId,
        String editUrl,
        String responseUrl,
        Long createdBy,
        Instant createdAt,
        Instant updatedAt,
        SurveySheetLinkResponse sheetLink
) {

    public static SurveyFormDetailResponse from(
            SurveyFormDetailResult result
    ) {
        return new SurveyFormDetailResponse(
                result.surveyFormId(),
                result.title(),
                result.dueAt(),
                result.status(),
                result.googleFormId(),
                result.editUrl(),
                result.responseUrl(),
                result.createdBy(),
                result.createdAt(),
                result.updatedAt(),
                SurveySheetLinkResponse.from(
                        result.sheetLink()
                )
        );
    }
}
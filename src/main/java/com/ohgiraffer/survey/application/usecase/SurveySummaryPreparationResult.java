package com.ohgiraffer.survey.application.usecase;

import com.ohgiraffer.survey.application.summary.SurveyAiSummary;
import com.ohgiraffer.survey.application.summary.SurveyStatisticsResult;

import java.time.Instant;

public record SurveySummaryPreparationResult(
        Long surveyFormId,
        String surveyTitle,
        Instant generatedAt,
        String fileName,
        SurveyStatisticsResult statistics,
        SurveyAiSummary aiSummary
) {

    public SurveySummaryPreparationResult {
        if (surveyFormId == null
                || surveyFormId <= 0) {
            throw new IllegalArgumentException(
                    "설문 폼 ID가 올바르지 않습니다."
            );
        }

        if (surveyTitle == null
                || surveyTitle.isBlank()) {
            throw new IllegalArgumentException(
                    "설문 제목이 필요합니다."
            );
        }

        if (generatedAt == null) {
            throw new IllegalArgumentException(
                    "요약 생성 시간이 필요합니다."
            );
        }

        if (fileName == null
                || fileName.isBlank()) {
            throw new IllegalArgumentException(
                    "PDF 파일명이 필요합니다."
            );
        }

        if (statistics == null) {
            throw new IllegalArgumentException(
                    "설문 통계 결과가 필요합니다."
            );
        }

        if (aiSummary == null) {
            aiSummary =
                    SurveyAiSummary.unavailable();
        }
    }
}
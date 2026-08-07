package com.ohgiraffer.survey.application.port;

import com.ohgiraffer.survey.application.summary.SurveyAiSummary;
import com.ohgiraffer.survey.application.summary.SurveyStatisticsResult;

public interface SurveySummaryAiPort {

    SurveyAiSummary summarize(
            String surveyTitle,
            SurveyStatisticsResult statistics
    );
}
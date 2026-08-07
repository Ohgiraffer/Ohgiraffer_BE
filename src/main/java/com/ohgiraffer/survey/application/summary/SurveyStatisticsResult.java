package com.ohgiraffer.survey.application.summary;

import java.util.List;

public record SurveyStatisticsResult(
        String spreadsheetTitle,
        String sheetName,
        int totalResponseCount,
        int totalQuestionCount,
        List<SurveyQuestionStatistics> questions
) {

    public SurveyStatisticsResult {
        questions = questions == null
                ? List.of()
                : List.copyOf(questions);
    }

    public List<SurveyQuestionStatistics> numericQuestions() {
        return questions.stream()
                .filter(
                        SurveyQuestionStatistics::isNumeric
                )
                .toList();
    }

    public List<SurveyQuestionStatistics> choiceQuestions() {
        return questions.stream()
                .filter(
                        SurveyQuestionStatistics::isChoice
                )
                .toList();
    }

    public List<SurveyQuestionStatistics> textQuestions() {
        return questions.stream()
                .filter(
                        SurveyQuestionStatistics::isText
                )
                .toList();
    }

    public boolean hasResponses() {
        return totalResponseCount > 0;
    }

    public boolean hasQuestions() {
        return totalQuestionCount > 0;
    }
}
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

    /*
     * PDF 상세 문항 영역에 표시할 문항입니다.
     * 점수형과 선택형만 표시합니다.
     */
    public List<SurveyQuestionStatistics> reportQuestions() {
        return questions.stream()
                .filter(question ->
                        question.isNumeric()
                                || question.isChoice()
                )
                .toList();
    }

    public boolean hasTextQuestions() {
        return questions.stream()
                .anyMatch(
                        SurveyQuestionStatistics::isText
                );
    }

    public boolean hasReportQuestions() {
        return questions.stream()
                .anyMatch(question ->
                        question.isNumeric()
                                || question.isChoice()
                );
    }

    public boolean hasResponses() {
        return totalResponseCount > 0;
    }

    public boolean hasQuestions() {
        return totalQuestionCount > 0;
    }
}
package com.ohgiraffer.survey.application.summary;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record SurveyQuestionStatistics(
        int questionNumber,
        String question,
        SurveyQuestionType type,
        int responseCount,
        BigDecimal average,
        BigDecimal minimum,
        BigDecimal maximum,
        Map<String, Long> distribution,
        List<String> textResponses
) {

    public SurveyQuestionStatistics {
        distribution = distribution == null
                ? Map.of()
                : Collections.unmodifiableMap(
                new LinkedHashMap<>(
                        distribution
                )
        );

        textResponses = textResponses == null
                ? List.of()
                : List.copyOf(textResponses);
    }

    public boolean isNumeric() {
        return type == SurveyQuestionType.NUMERIC;
    }

    public boolean isChoice() {
        return type == SurveyQuestionType.CHOICE;
    }

    public boolean isText() {
        return type == SurveyQuestionType.TEXT;
    }

    public boolean isUnanswered() {
        return type == SurveyQuestionType.UNANSWERED;
    }

}
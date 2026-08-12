package com.ohgiraffer.survey.application.summary;

import com.ohgiraffer.survey.application.port.SurveyResponseDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
public class SurveyStatisticsCalculator {

    private static final Logger log =
            LoggerFactory.getLogger(
                    SurveyStatisticsCalculator.class
            );

    private static final int AVERAGE_SCALE = 2;

    private static final int MAX_CHOICE_COUNT = 10;

    private static final int MIN_CHOICE_RESPONSE_COUNT = 3;

    private static final int MAX_TEXT_RESPONSES = 200;

    private static final int MAX_TEXT_LENGTH = 500;

    public SurveyStatisticsResult calculate(
            SurveyResponseDataset dataset
    ) {
        if (dataset == null) {
            throw new IllegalArgumentException(
                    "설문 응답 데이터가 필요합니다."
            );
        }

        List<SurveyQuestionStatistics> questions =
                new ArrayList<>();

        int questionNumber = 1;

        for (int columnIndex = 0;
             columnIndex < dataset.headers().size();
             columnIndex++) {

            String header =
                    dataset.headers().get(columnIndex);

            if (header == null || header.isBlank()) {
                continue;
            }

            List<String> responses =
                    collectResponses(
                            dataset.rows(),
                            columnIndex
                    );

            SurveyQuestionStatistics statistics;

            if (responses.isEmpty()) {
                statistics =
                        createUnansweredQuestion(
                                questionNumber,
                                header
                        );
            } else {
                statistics =
                        analyzeSafely(
                                questionNumber,
                                header,
                                responses
                        );
            }

            questions.add(statistics);
            questionNumber++;
        }

        return new SurveyStatisticsResult(
                dataset.spreadsheetTitle(),
                dataset.sheetName(),
                dataset.responseCount(),
                questions.size(),
                questions
        );
    }

    private SurveyQuestionStatistics createUnansweredQuestion(
            int questionNumber,
            String question
    ) {
        return new SurveyQuestionStatistics(
                questionNumber,
                question,
                SurveyQuestionType.UNANSWERED,
                0,
                null,
                null,
                null,
                Map.of(),
                List.of()
        );
    }

    private SurveyQuestionStatistics analyzeSafely(
            int questionNumber,
            String question,
            List<String> responses
    ) {
        try {
            return analyzeQuestion(
                    questionNumber,
                    question,
                    responses
            );
        } catch (RuntimeException exception) {
            log.warn(
                    "설문 문항 통계 계산에 실패하여 "
                            + "서술형 문항으로 처리합니다. "
                            + "questionNumber={}, question={}",
                    questionNumber,
                    question,
                    exception
            );

            return analyzeTextQuestion(
                    questionNumber,
                    question,
                    responses
            );
        }
    }

    private SurveyQuestionStatistics analyzeQuestion(
            int questionNumber,
            String question,
            List<String> responses
    ) {
        if (isNumericQuestion(responses)) {
            return analyzeNumericQuestion(
                    questionNumber,
                    question,
                    responses
            );
        }

        if (isChoiceQuestion(responses)) {
            return analyzeChoiceQuestion(
                    questionNumber,
                    question,
                    responses
            );
        }

        return analyzeTextQuestion(
                questionNumber,
                question,
                responses
        );
    }

    private SurveyQuestionStatistics analyzeNumericQuestion(
            int questionNumber,
            String question,
            List<String> responses
    ) {
        List<BigDecimal> numbers =
                responses.stream()
                        .map(this::parseNumber)
                        .toList();

        BigDecimal sum =
                numbers.stream()
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal average =
                sum.divide(
                        BigDecimal.valueOf(
                                numbers.size()
                        ),
                        AVERAGE_SCALE,
                        RoundingMode.HALF_UP
                );

        BigDecimal minimum =
                numbers.stream()
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

        BigDecimal maximum =
                numbers.stream()
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

        Map<String, Long> distribution =
                createDistribution(
                        responses
                );

        return new SurveyQuestionStatistics(
                questionNumber,
                question,
                SurveyQuestionType.NUMERIC,
                responses.size(),
                average,
                minimum,
                maximum,
                distribution,
                List.of()
        );
    }

    private SurveyQuestionStatistics analyzeChoiceQuestion(
            int questionNumber,
            String question,
            List<String> responses
    ) {
        Map<String, Long> distribution =
                createDistribution(
                        responses
                );

        return new SurveyQuestionStatistics(
                questionNumber,
                question,
                SurveyQuestionType.CHOICE,
                responses.size(),
                null,
                null,
                null,
                distribution,
                List.of()
        );
    }

    private SurveyQuestionStatistics analyzeTextQuestion(
            int questionNumber,
            String question,
            List<String> responses
    ) {
        List<String> limitedResponses =
                responses.stream()
                        .limit(MAX_TEXT_RESPONSES)
                        .map(this::limitTextLength)
                        .toList();

        return new SurveyQuestionStatistics(
                questionNumber,
                question,
                SurveyQuestionType.TEXT,
                responses.size(),
                null,
                null,
                null,
                Map.of(),
                limitedResponses
        );
    }

    private List<String> collectResponses(
            List<List<String>> rows,
            int columnIndex
    ) {
        List<String> responses =
                new ArrayList<>();

        for (List<String> row : rows) {
            if (row == null
                    || columnIndex >= row.size()) {
                continue;
            }

            String value = row.get(columnIndex);

            if (value == null || value.isBlank()) {
                continue;
            }

            responses.add(value.trim());
        }

        return List.copyOf(responses);
    }

    private boolean isNumericQuestion(
            List<String> responses
    ) {
        if (responses.isEmpty()) {
            return false;
        }

        return responses.stream()
                .allMatch(this::isNumber);
    }

    private boolean isChoiceQuestion(
            List<String> responses
    ) {
        if (responses.size()
                < MIN_CHOICE_RESPONSE_COUNT) {
            return false;
        }

        long distinctCount =
                responses.stream()
                        .distinct()
                        .count();

        int relativeLimit =
                Math.max(
                        2,
                        (int) Math.ceil(
                                responses.size() * 0.5
                        )
                );

        return distinctCount <= MAX_CHOICE_COUNT
                && distinctCount <= relativeLimit;
    }

    private boolean isSensitiveMetadataColumn(
            String header
    ) {
        String normalized =
                header.trim()
                        .toLowerCase(Locale.ROOT)
                        .replace(" ", "");

        return normalized.equals("이메일")
                || normalized.equals("이메일주소")
                || normalized.equals("email")
                || normalized.equals("emailaddress");
    }

    private boolean isNumber(
            String value
    ) {
        try {
            parseNumber(value);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private BigDecimal parseNumber(
            String value
    ) {
        String normalized =
                value.trim()
                        .replace(",", "");

        return new BigDecimal(normalized);
    }

    private Map<String, Long> createDistribution(
            List<String> responses
    ) {
        Map<String, Long> distribution =
                new LinkedHashMap<>();

        for (String response : responses) {
            distribution.merge(
                    response,
                    1L,
                    Long::sum
            );
        }

        return distribution;
    }

    private String limitTextLength(
            String value
    ) {
        if (value == null) {
            return "";
        }

        if (value.length() <= MAX_TEXT_LENGTH) {
            return value;
        }

        return value.substring(
                0,
                MAX_TEXT_LENGTH
        ) + "...";
    }
}
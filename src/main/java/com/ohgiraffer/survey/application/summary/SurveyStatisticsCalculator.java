package com.ohgiraffer.survey.application.summary;

import com.ohgiraffer.survey.application.port.SurveyResponseDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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

    /*
     * 이메일과 전화번호는 AI에 전달하기 전에 제거합니다.
     * 주관식 응답에 개인정보가 포함될 가능성을 줄이기 위한 처리입니다.
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
            );

    private static final Pattern PHONE_PATTERN =
            Pattern.compile(
                    "(?:\\+?82[-.\\s]?)?0?1[016789][- .]?\\d{3,4}[- .]?\\d{4}"
            );

    /*
     * Google Form 응답 시트에 자동 또는 사용자 입력으로 포함될 수 있는
     * 개인정보/응답자 메타데이터 컬럼입니다.
     *
     * 이 컬럼은 통계, AI 요청, PDF에서 모두 제외합니다.
     */
    private static final Set<String> EXCLUDED_HEADERS =
            Set.of(
                    "타임스탬프",
                    "응답일시",
                    "제출일시",
                    "제출시간",
                    "이메일",
                    "이메일주소",
                    "응답자이메일",
                    "email",
                    "emailaddress",
                    "이름",
                    "성명",
                    "응답자이름",
                    "전화번호",
                    "휴대전화",
                    "휴대폰번호",
                    "학번",
                    "주소",
                    "소속팀",
                    "팀명",
                    "트랙",
                    "모듈",
                    "반",
                    "기수"
            );

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

            /*
             * 제목이 없거나 개인정보/메타데이터 컬럼이면
             * 문항으로 만들지 않습니다.
             */
            if (header == null
                    || header.isBlank()
                    || isSensitiveMetadataColumn(header)) {
                continue;
            }

            List<String> responses =
                    collectResponses(
                            dataset.rows(),
                            columnIndex
                    );

            /*
             * 응답이 한 건도 없는 문항은
             * AI 입력과 PDF에서 완전히 제외합니다.
             */
            if (responses.isEmpty()) {
                continue;
            }

            SurveyQuestionStatistics statistics =
                    analyzeSafely(
                            questionNumber,
                            header.trim(),
                            responses
                    );

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
                        BigDecimal.valueOf(numbers.size()),
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
                createDistribution(responses);

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
                createDistribution(responses);

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
        /*
         * 주관식 원문은 PDF에 직접 표시하지 않습니다.
         * 개인정보를 제거한 결과만 AI 요약 입력으로 전달합니다.
         */
        List<String> sanitizedResponses =
                responses.stream()
                        .map(this::sanitizeTextResponse)
                        .filter(value -> !value.isBlank())
                        .limit(MAX_TEXT_RESPONSES)
                        .map(this::limitTextLength)
                        .toList();

        return new SurveyQuestionStatistics(
                questionNumber,
                question,
                SurveyQuestionType.TEXT,
                sanitizedResponses.size(),
                null,
                null,
                null,
                Map.of(),
                sanitizedResponses
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

            String value =
                    row.get(columnIndex);

            if (value == null
                    || value.isBlank()) {
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
                normalizeHeader(header);

        if (EXCLUDED_HEADERS.contains(normalized)) {
            return true;
        }

        /*
         * "이름을 기재해주세요", "이메일을 입력해주세요"처럼
         * 문장 형태로 작성된 개인정보 컬럼도 제외합니다.
         */
        return normalized.contains("이메일")
                || normalized.contains("emailaddress")
                || normalized.contains("응답자email")
                || normalized.contains("이름을기재")
                || normalized.contains("이름을입력")
                || normalized.contains("성명을기재")
                || normalized.contains("성명을입력")
                || normalized.contains("전화번호를입력")
                || normalized.contains("휴대전화번호")
                || normalized.contains("타임스탬프");
    }

    private String normalizeHeader(
            String header
    ) {
        return header.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\s_\\-()\\[\\]{}:]", "");
    }

    private String sanitizeTextResponse(
            String value
    ) {
        if (value == null
                || value.isBlank()) {
            return "";
        }

        String sanitized =
                EMAIL_PATTERN.matcher(value.trim())
                        .replaceAll("[이메일 제거]");

        sanitized =
                PHONE_PATTERN.matcher(sanitized)
                        .replaceAll("[전화번호 제거]");

        return sanitized;
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

        /*
         * Unicode 코드 포인트 기준으로 잘라서
         * 이모지나 보조 평면 문자가 깨지지 않도록 합니다.
         */
        int endIndex =
                value.offsetByCodePoints(
                        0,
                        Math.min(
                                MAX_TEXT_LENGTH,
                                value.codePointCount(
                                        0,
                                        value.length()
                                )
                        )
                );

        return value.substring(
                0,
                endIndex
        ) + "...";
    }
}
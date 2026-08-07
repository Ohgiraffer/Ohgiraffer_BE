package com.ohgiraffer.survey.infrastructure.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.survey.application.summary.SurveyQuestionStatistics;
import com.ohgiraffer.survey.application.summary.SurveyStatisticsResult;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class SurveySummaryPromptBuilder {

    private static final int
            MAX_TEXT_RESPONSES_PER_QUESTION = 50;

    private static final int
            MAX_TEXT_RESPONSE_LENGTH = 300;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "[A-Za-z0-9._%+-]+"
                            + "@"
                            + "[A-Za-z0-9.-]+"
                            + "\\.[A-Za-z]{2,}"
            );

    private static final Pattern PHONE_PATTERN =
            Pattern.compile(
                    "(?<!\\d)"
                            + "(?:01[016789])"
                            + "[-\\s]?"
                            + "\\d{3,4}"
                            + "[-\\s]?"
                            + "\\d{4}"
                            + "(?!\\d)"
            );

    private final ObjectMapper objectMapper;

    public SurveySummaryPromptBuilder(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    public String build(
            String surveyTitle,
            SurveyStatisticsResult statistics
    ) {
        Map<String, Object> payload =
                new LinkedHashMap<>();

        payload.put(
                "surveyTitle",
                surveyTitle
        );

        payload.put(
                "totalResponseCount",
                statistics.totalResponseCount()
        );

        payload.put(
                "totalQuestionCount",
                statistics.totalQuestionCount()
        );

        payload.put(
                "questions",
                statistics.questions()
                        .stream()
                        .map(this::toQuestionPayload)
                        .toList()
        );

        String statisticsJson =
                writeJson(payload);

        return """
        당신은 교육 프로그램의 설문 결과를 분석하는
        전문 데이터 분석가입니다.

        아래 설문 통계와 익명화된 응답을 바탕으로
        운영진이 바로 활용할 수 있는 한국어 요약을
        작성하세요.

        반드시 지켜야 할 규칙:
        1. 입력 데이터에 없는 사실을 만들지 마세요.
        2. 개인을 식별하거나 특정 응답자를 추측하지 마세요.
        3. 이메일, 전화번호, 이름 등 개인정보를 출력하지 마세요.
        4. 표본이 적으면 단정하지 말고
           '응답 수가 적어 해석에 주의가 필요함'이라고 표현하세요.
        5. 숫자 통계는 제공된 값과 일치해야 합니다.
        6. 강점과 개선점을 구체적이고 간결하게 작성하세요.
        7. 개선 권고사항은 실제로 실행할 수 있는 내용으로 작성하세요.
        8. 모든 내용은 한국어로 작성하세요.
        9. Markdown 문법과 코드 블록을 사용하지 마세요.
        10. JSON 이외의 설명을 앞뒤에 추가하지 마세요.

        반드시 아래 JSON 구조로만 응답하세요.

        {
          "overview": "전체 설문 결과 요약",
          "keyInsights": [
            "핵심 인사이트"
          ],
          "strengths": [
            "긍정적인 결과 또는 강점"
          ],
          "improvements": [
            "개선이 필요한 사항"
          ],
          "recommendations": [
            "실행 가능한 개선 권고사항"
          ],
          "questionSummaries": [
            {
              "questionNumber": 1,
              "summary": "해당 문항의 핵심 결과 요약"
            }
          ]
        }

        분석 대상 데이터:
        """
                + statisticsJson;
    }

    private Map<String, Object> toQuestionPayload(
            SurveyQuestionStatistics question
    ) {
        Map<String, Object> payload =
                new LinkedHashMap<>();

        payload.put(
                "questionNumber",
                question.questionNumber()
        );

        payload.put(
                "question",
                question.question()
        );

        payload.put(
                "type",
                question.type().name()
        );

        payload.put(
                "responseCount",
                question.responseCount()
        );

        if (question.isNumeric()) {
            payload.put(
                    "average",
                    question.average()
            );

            payload.put(
                    "minimum",
                    question.minimum()
            );

            payload.put(
                    "maximum",
                    question.maximum()
            );

            payload.put(
                    "distribution",
                    question.distribution()
            );
        }

        if (question.isChoice()) {
            payload.put(
                    "distribution",
                    question.distribution()
            );
        }

        if (question.isText()) {
            List<String> responses =
                    question.textResponses()
                            .stream()
                            .limit(
                                    MAX_TEXT_RESPONSES_PER_QUESTION
                            )
                            .map(this::sanitizeText)
                            .toList();

            payload.put(
                    "responses",
                    responses
            );
        }

        return payload;
    }

    private String sanitizeText(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String sanitized =
                EMAIL_PATTERN.matcher(value)
                        .replaceAll("[이메일 제거]");

        sanitized =
                PHONE_PATTERN.matcher(sanitized)
                        .replaceAll("[전화번호 제거]");

        if (sanitized.length()
                > MAX_TEXT_RESPONSE_LENGTH) {
            sanitized = sanitized.substring(
                    0,
                    MAX_TEXT_RESPONSE_LENGTH
            ) + "...";
        }

        return sanitized;
    }

    private String writeJson(
            Map<String, Object> payload
    ) {
        try {
            return objectMapper.writeValueAsString(
                    payload
            );
        } catch (JsonProcessingException exception) {
            throw new BusinessException(
                    ErrorCode.AI_API_CALL_FAILED
            );
        }
    }
}
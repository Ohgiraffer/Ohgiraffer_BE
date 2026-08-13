package com.ohgiraffer.survey.infrastructure.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffer.ai.infrastructure.gemini.GeminiClient;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.survey.application.port.SurveySummaryAiPort;
import com.ohgiraffer.survey.application.summary.SurveyAiSummary;
import com.ohgiraffer.survey.application.summary.SurveyStatisticsResult;
import com.ohgiraffer.survey.application.summary.SurveyQuestionStatistics;
import org.springframework.web.client.RestClientException;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GeminiSurveySummaryAdapter implements SurveySummaryAiPort {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;
    private final SurveySummaryPromptBuilder promptBuilder;
    private static final Logger log =
            LoggerFactory.getLogger(
                    GeminiSurveySummaryAdapter.class
            );


    public GeminiSurveySummaryAdapter(
            GeminiClient geminiClient,
            ObjectMapper objectMapper,
            SurveySummaryPromptBuilder promptBuilder
    ) {
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
        this.promptBuilder = promptBuilder;
    }

    @Override
    public SurveyAiSummary summarize(
            String surveyTitle,
            SurveyStatisticsResult statistics
    ) {
        String stage = "BUILD_PROMPT";

        try {
            String prompt =
                    promptBuilder.build(
                            surveyTitle,
                            statistics
                    );

            stage = "CALL_GEMINI_API";

            String responseText =
                    geminiClient.generateText(
                            prompt
                    );

            stage = "EXTRACT_JSON";

            String responseJson =
                    extractJson(
                            responseText
                    );

            stage = "PARSE_JSON";

            GeminiSurveySummaryResponse response =
                    objectMapper.readValue(
                            responseJson,
                            GeminiSurveySummaryResponse.class
                    );

            stage = "VALIDATE_RESPONSE";

            validateResponse(response);

            stage = "CONVERT_RESPONSE";

            return SurveyAiSummary.success(
                    response.overview(),
                    response.keyInsights(),
                    response.strengths(),
                    response.improvements(),
                    response.recommendations(),
                    convertQuestionSummaries(
                            response.questionSummaries(),
                            statistics
                    )
            );

        } catch (BusinessException exception) {
            log.warn(
                    "Gemini 설문 요약 처리 실패. stage={}, errorCode={}",
                    stage,
                    exception.getErrorCode(),
                    exception
            );

            if (exception.getErrorCode()
                    == ErrorCode.AI_API_CALL_FAILED) {
                throw exception;
            }

            throw new BusinessException(
                    ErrorCode.AI_API_CALL_FAILED,
                    exception
            );

        } catch (
                JsonProcessingException
                | RestClientException exception
        ) {
            log.warn(
                    "Gemini 설문 요약 처리 실패. stage={}",
                    stage,
                    exception
            );

            throw new BusinessException(
                    ErrorCode.AI_API_CALL_FAILED,
                    exception
            );

        } catch (RuntimeException exception) {
            log.warn(
                    "Gemini 설문 요약 처리 중 예상하지 못한 오류. stage={}",
                    stage,
                    exception
            );

            throw new BusinessException(
                    ErrorCode.AI_API_CALL_FAILED,
                    exception
            );
        }
    }

    private String extractJson(
            String responseText
    ) {
        if (responseText == null
                || responseText.isBlank()) {
            throw new BusinessException(
                    ErrorCode.AI_API_CALL_FAILED
            );
        }

        String normalized =
                responseText.trim();

        if (normalized.startsWith("```")) {
            int firstLineEnd =
                    normalized.indexOf('\n');

            if (firstLineEnd >= 0) {
                normalized = normalized.substring(
                        firstLineEnd + 1
                );
            }

            int closingCodeBlock =
                    normalized.lastIndexOf("```");

            if (closingCodeBlock >= 0) {
                normalized = normalized.substring(
                        0,
                        closingCodeBlock
                );
            }

            normalized = normalized.trim();
        }

        int jsonStart =
                normalized.indexOf('{');

        int jsonEnd =
                normalized.lastIndexOf('}');

        if (jsonStart < 0
                || jsonEnd < jsonStart) {
            throw new BusinessException(
                    ErrorCode.AI_API_CALL_FAILED
            );
        }

        return normalized.substring(
                jsonStart,
                jsonEnd + 1
        );
    }

    private List<SurveyAiSummary.QuestionSummary>
    convertQuestionSummaries(
            List<GeminiSurveySummaryResponse
                    .QuestionSummaryResponse> responses,
            SurveyStatisticsResult statistics
    ) {
        if (responses == null || responses.isEmpty()) {
            return List.of();
        }

        Set<Integer> validQuestionNumbers =
                statistics.questions()
                        .stream()
                        .map(
                                SurveyQuestionStatistics::questionNumber
                        )
                        .collect(
                                Collectors.toUnmodifiableSet()
                        );

        Set<Integer> seenQuestionNumbers =
                new HashSet<>();

        return responses.stream()
                .filter(response ->
                        response != null
                                && validQuestionNumbers.contains(
                                response.questionNumber()
                        )
                                && seenQuestionNumbers.add(
                                response.questionNumber()
                        )
                                && response.summary() != null
                                && !response.summary().isBlank()
                )
                .map(response ->
                        new SurveyAiSummary.QuestionSummary(
                                response.questionNumber(),
                                response.summary()
                        )
                )
                .toList();
    }

    private void validateResponse(
            GeminiSurveySummaryResponse response
    ) {
        if (response == null
                || response.overview() == null
                || response.overview().isBlank()) {
            throw new BusinessException(
                    ErrorCode.AI_API_CALL_FAILED
            );
        }
    }


}
package com.ohgiraffer.survey.infrastructure.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffer.ai.infrastructure.gemini.GeminiClient;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.survey.application.port.SurveySummaryAiPort;
import com.ohgiraffer.survey.application.summary.SurveyAiSummary;
import com.ohgiraffer.survey.application.summary.SurveyStatisticsResult;
import org.springframework.web.client.RestClientException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeminiSurveySummaryAdapter
        implements SurveySummaryAiPort {

    private final GeminiClient geminiClient;

    private final ObjectMapper objectMapper;

    private final SurveySummaryPromptBuilder promptBuilder;

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
        String prompt =
                promptBuilder.build(
                        surveyTitle,
                        statistics
                );

        try {
            String responseText =
                    geminiClient.generateText(
                            prompt
                    );

            String responseJson =
                    extractJson(
                            responseText
                    );

            GeminiSurveySummaryResponse response =
                    objectMapper.readValue(
                            responseJson,
                            GeminiSurveySummaryResponse.class
                    );

            validateResponse(response);

            return SurveyAiSummary.success(
                    response.overview(),
                    response.keyInsights(),
                    response.strengths(),
                    response.improvements(),
                    response.recommendations(),
                    convertQuestionSummaries(
                            response.questionSummaries()
                    )
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (
                JsonProcessingException
                | RestClientException exception
        ) {
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
                    .QuestionSummaryResponse> responses
    ) {
        if (responses == null) {
            return List.of();
        }

        return responses.stream()
                .filter(response ->
                        response != null
                                && response.questionNumber() > 0
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
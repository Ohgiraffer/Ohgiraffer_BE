package com.ohgiraffer.ai.infrastructure.gemini;

import com.ohgiraffer.ai.application.recorder.AiUsageRecorder;
import com.ohgiraffer.ai.domain.model.FailReason;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    private final RestClient restClient;
    private final GeminiProperties properties;
    private final AiUsageRecorder aiUsageRecorder;

    public GeminiClient(GeminiProperties properties, AiUsageRecorder aiUsageRecorder) {
        this.properties = properties;
        this.aiUsageRecorder = aiUsageRecorder;

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(
                Duration.ofSeconds(properties.getTimeoutSeconds())
        );
        requestFactory.setReadTimeout(
                Duration.ofSeconds(properties.getTimeoutSeconds())
        );

        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    public String generateText(
            String prompt
    ) {
        return generateText(
                null,
                prompt
        );
    }

    public String generateText(
            String systemInstruction,
            String userPrompt
    ) {
        if (userPrompt == null
                || userPrompt.isBlank()) {
            throw new BusinessException(
                    ErrorCode.AI_API_CALL_FAILED
            );
        }

        Map<String, Object> body;

        if (systemInstruction == null
                || systemInstruction.isBlank()) {

            body = Map.of(
                    "contents",
                    createUserContents(userPrompt)
            );

        } else {
            body = Map.of(
                    "systemInstruction",
                    Map.of(
                            "parts",
                            List.of(
                                    Map.of(
                                            "text",
                                            systemInstruction
                                    )
                            )
                    ),
                    "contents",
                    createUserContents(userPrompt)
            );
        }

        Map<?, ?> response = callGemini(body);
        String text;
        try {
            text = extractText(response);
        } catch (BusinessException e) {
            aiUsageRecorder.recordFailure(resolveFeatureName(), properties.getModel(), FailReason.EMPTY_RESPONSE);
            throw e;
        }
        recordUsage(response);
        return text;
    }

    private List<Map<String, Object>> createUserContents(
            String userPrompt
    ) {
        return List.of(
                Map.of(
                        "role",
                        "user",
                        "parts",
                        List.of(
                                Map.of(
                                        "text",
                                        userPrompt
                                )
                        )
                )
        );
    }

    private String extractText(Map<?, ?> response) {
        if (response == null) {
            throw new BusinessException(ErrorCode.AI_API_CALL_FAILED);
        }

        List<?> candidates = asList(response.get("candidates"));

        if (candidates.isEmpty()) {
            throw new BusinessException(ErrorCode.AI_API_CALL_FAILED);
        }

        Map<?, ?> candidate = asMap(candidates.get(0));
        Map<?, ?> content = asMap(candidate.get("content"));
        List<?> parts = asList(content.get("parts"));

        if (parts.isEmpty()) {
            throw new BusinessException(ErrorCode.AI_API_CALL_FAILED);
        }

        Map<?, ?> part = asMap(parts.get(0));
        Object text = part.get("text");

        if (!(text instanceof String result) || result.isBlank()) {
            throw new BusinessException(ErrorCode.AI_API_CALL_FAILED);
        }

        return result;
    }

    private Map<?, ?> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return map;
        }

        throw new BusinessException(ErrorCode.AI_API_CALL_FAILED);
    }

    private List<?> asList(Object value) {
        if (value instanceof List<?> list) {
            return list;
        }

        throw new BusinessException(ErrorCode.AI_API_CALL_FAILED);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> generateWithTools(List<Map<String, Object>> contents, List<Map<String, Object>> tools) {
        Map<String, Object> body = Map.of(
                "contents", contents,
                "tools", tools
        );

        Map<?, ?> response = callGemini(body);

        recordUsage(response);

        return (Map<String, Object>) response;
    }

    private Map<?, ?> callGemini(Map<String, Object> body) {
        try {
            Map<?, ?> response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/{model}:generateContent")
                            .queryParam("key", properties.getApiKey())
                            .build(properties.getModel()))
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                aiUsageRecorder.recordFailure(resolveFeatureName(), properties.getModel(), FailReason.EMPTY_RESPONSE);
                throw new BusinessException(ErrorCode.AI_API_CALL_FAILED);
            }

            return response;

        } catch (RestClientResponseException e) {
            FailReason reason = resolveFailReason(e.getStatusCode());
            aiUsageRecorder.recordFailure(resolveFeatureName(), properties.getModel(), reason);
            throw mapToBusinessException(reason);
        } catch (RestClientException e) {
            aiUsageRecorder.recordFailure(resolveFeatureName(), properties.getModel(), FailReason.SERVER_ERROR);
            throw new BusinessException(ErrorCode.AI_API_CALL_FAILED);
        }
    }

    private FailReason resolveFailReason(HttpStatusCode status) {
        if (status.value() == 429) {
            return FailReason.RATE_LIMIT;
        }
        if (status.value() == 401 || status.value() == 403) {
            return FailReason.AUTH_INVALID;
        }
        if (status.value() == 400) {
            return FailReason.BAD_REQUEST;
        }
        return FailReason.SERVER_ERROR;
    }

    private BusinessException mapToBusinessException(FailReason reason) {
        return switch (reason) {
            case RATE_LIMIT -> new BusinessException(ErrorCode.AI_RATE_LIMIT_EXCEEDED);
            case AUTH_INVALID -> new BusinessException(ErrorCode.AI_API_KEY_INVALID);
            case BAD_REQUEST -> new BusinessException(ErrorCode.AI_REQUEST_INVALID);
            default -> new BusinessException(ErrorCode.AI_API_CALL_FAILED);
        };
    }

    private void recordUsage(Map<?, ?> response) {
        if (response == null) {
            return;
        }

        Object usageObj = response.get("usageMetadata");
        if (!(usageObj instanceof Map<?, ?> usage)) {
            return;
        }

        aiUsageRecorder.recordSuccess(
                resolveFeatureName(),
                properties.getModel(),
                toInt(usage.get("promptTokenCount")),
                toInt(usage.get("candidatesTokenCount")),
                toInt(usage.get("cachedContentTokenCount"))
        );
    }

    private int toInt(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private String resolveFeatureName() {
        StackWalker.StackFrame callerFrame = StackWalker.getInstance()
                .walk(frames -> frames
                        .filter(f -> !f.getClassName().equals(GeminiClient.class.getName()))
                        .findFirst()
                        .orElse(null));

        return callerFrame != null ? callerFrame.getClassName() : "unknown";
    }

}
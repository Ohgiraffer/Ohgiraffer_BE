package com.ohgiraffer.ai.infrastructure.gemini;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta";

    private final RestClient restClient;
    private final GeminiProperties properties;

    public GeminiClient(GeminiProperties properties) {
        this.properties = properties;

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(
                Duration.ofSeconds(properties.getTimeoutSeconds())
        );
        requestFactory.setReadTimeout(
                Duration.ofSeconds(properties.getTimeoutSeconds())
        );

        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
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

    /*
     * systemInstruction과 사용자 데이터를 분리하여 전송합니다.
     *
     * systemInstruction:
     * - AI가 반드시 따라야 하는 고정 규칙
     *
     * userPrompt:
     * - 실제 분석 대상 데이터
     */
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

        Map<?, ?> response =
                restClient.post()
                        .uri(uriBuilder ->
                                uriBuilder
                                        .path(
                                                "/models/{model}:generateContent"
                                        )
                                        .queryParam(
                                                "key",
                                                properties.getApiKey()
                                        )
                                        .build(
                                                properties.getModel()
                                        )
                        )
                        .body(body)
                        .retrieve()
                        .body(Map.class);

        return extractText(response);
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

    // 챗봇 전용 - function-calling 지원. contents는 대화 히스토리(user/model/function role 섞임), tools는 role별 필터링된 함수 목록
    @SuppressWarnings("unchecked")
    public Map<String, Object> generateWithTools(List<Map<String, Object>> contents, List<Map<String, Object>> tools) {
        Map<String, Object> body = Map.of(
                "contents", contents,
                "tools", tools
        );

        Map<String, Object> response = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/models/{model}:generateContent")
                        .queryParam("key", properties.getApiKey())
                        .build(properties.getModel()))
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new BusinessException(ErrorCode.AI_API_CALL_FAILED);
        }
        return response;

    }

}
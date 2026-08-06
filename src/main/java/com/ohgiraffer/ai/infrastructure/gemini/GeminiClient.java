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

    public String generateText(String prompt) {
        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        Map<?, ?> response = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/models/{model}:generateContent")
                        .queryParam("key", properties.getApiKey())
                        .build(properties.getModel()))
                .body(body)
                .retrieve()
                .body(Map.class);

        return extractText(response);
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
}
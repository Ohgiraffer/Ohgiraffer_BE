package com.ohgiraffer.evaluation.infrastructure.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffer.ai.infrastructure.gemini.GeminiClient;
import com.ohgiraffer.evaluation.application.port.EvaluationSummaryPort;
import com.ohgiraffer.evaluation.domain.model.EvaluationChange;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 제미나이로 확인이 필요한 훈련생을 골라낸다.
 *
 * <p>팀 공용 {@link GeminiClient} 는 문자열만 돌려주므로 JSON 을 요청하고 여기서 읽는다.
 *
 * <p>모델이 형식을 어길 수 있다는 전제로 읽는다. 한 항목이 깨졌다고 전체를 버리면 나머지
 * 멀쩡한 지적까지 사라지므로, 읽을 수 없는 항목만 건너뛴다.
 */
@Component
public class GeminiEvaluationSummaryAdapter implements EvaluationSummaryPort {

    private static final Logger log =
            LoggerFactory.getLogger(GeminiEvaluationSummaryAdapter.class);

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public GeminiEvaluationSummaryAdapter(
            GeminiClient geminiClient,
            ObjectMapper objectMapper
    ) {
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, String> findPointsToCheck(List<EvaluationChange> changes) {
        if (changes.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "확인할 변경 내역이 없습니다."
            );
        }

        String answer = geminiClient.generateText(
                EvaluationSummaryPromptBuilder.build(changes));

        return toPoints(readArray(answer), traineeNamesOf(changes));
    }

    private JsonNode readArray(String answer) {
        try {
            JsonNode root = objectMapper.readTree(stripCodeFence(answer));

            if (!root.isArray()) {
                throw new BusinessException(ErrorCode.AI_API_CALL_FAILED);
            }

            return root;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("평가 확인 필요 응답을 읽지 못했습니다. 응답={}", answer, exception);
            throw new BusinessException(ErrorCode.AI_API_CALL_FAILED);
        }
    }

    /**
     * 코드 블록 표시를 붙이지 말라고 일러도 붙여 오는 경우가 있어 걷어낸다.
     */
    private String stripCodeFence(String answer) {
        String trimmed = answer.trim();

        if (!trimmed.startsWith("```")) {
            return trimmed;
        }

        int start = trimmed.indexOf('\n');
        int end = trimmed.lastIndexOf("```");

        if (start < 0 || end <= start) {
            return trimmed;
        }

        return trimmed.substring(start + 1, end).trim();
    }

    private Map<String, String> toPoints(JsonNode array, Set<String> traineeNames) {
        Map<String, String> points = new LinkedHashMap<>();

        for (JsonNode element : array) {
            String traineeName = text(element, "traineeName");
            String needsCheck = text(element, "needsCheck");

            if (traineeName == null || needsCheck == null) {
                continue;
            }

            /*
             * 이번 변경에 없는 이름은 버린다. 모델이 지어낸 이름을 그대로 화면에 올리면
             * 운영진이 있지도 않은 훈련생을 찾게 된다.
             */
            if (!traineeNames.contains(traineeName)) {
                log.warn("평가 확인 필요에서 이번 변경에 없는 이름을 받았습니다. 이름={}", traineeName);
                continue;
            }

            points.putIfAbsent(traineeName, needsCheck);
        }

        return points;
    }

    private Set<String> traineeNamesOf(List<EvaluationChange> changes) {
        return changes.stream()
                .map(EvaluationChange::traineeName)
                .collect(Collectors.toSet());
    }

    private String text(JsonNode element, String field) {
        if (!element.isObject()) {
            return null;
        }

        JsonNode value = element.get(field);

        if (value == null || value.isNull() || !value.isTextual()) {
            return null;
        }

        String trimmed = value.asText().trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}

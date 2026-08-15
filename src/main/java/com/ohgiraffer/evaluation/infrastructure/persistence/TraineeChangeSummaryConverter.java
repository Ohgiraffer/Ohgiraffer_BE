package com.ohgiraffer.evaluation.infrastructure.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffer.evaluation.domain.model.TraineeChangeSummary;

import java.util.List;

/**
 * 훈련생별 변경 카드를 {@code diff_summary} 컬럼에 넣고 뺀다.
 *
 * <p>카드마다 행을 따로 두지 않고 한 컬럼에 담는 이유는, 이 값이 그때의 기록일 뿐 따로
 * 조회하거나 조건으로 걸 일이 없기 때문이다. 이력 상세에서 통째로 읽어 화면에 그린다.
 *
 * <p>{@code diff_summary} 에는 예전에 글 한 덩어리가 들어 있었다. 그 시절 기록을 읽으면
 * JSON 이 아니라 파싱에 실패하는데, 그때는 빈 목록으로 본다. 이력 하나 때문에 목록 전체가
 * 열리지 않는 편이 더 나쁘다.
 */
final class TraineeChangeSummaryConverter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final TypeReference<List<TraineeChangeSummary>> TYPE =
            new TypeReference<>() {
            };

    private TraineeChangeSummaryConverter() {
    }

    static String toJson(List<TraineeChangeSummary> summaries) {
        if (summaries == null || summaries.isEmpty()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(summaries);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "동기화 이력 요약을 저장할 수 없습니다.", exception);
        }
    }

    static List<TraineeChangeSummary> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            return OBJECT_MAPPER.readValue(json, TYPE);
        } catch (Exception exception) {
            return List.of();
        }
    }
}

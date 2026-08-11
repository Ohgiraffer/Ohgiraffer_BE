package com.ohgiraffer.evaluation.infrastructure.persistence;

import com.ohgiraffer.evaluation.domain.model.EvaluationColumnMapping;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * 컬럼 매핑을 DB 의 JSON 컬럼과 주고받는다.
 *
 * <p>{@code external_sheet_link.column_mapping} 이 JSON 이라 문자열로 오간다.
 * 매핑 항목은 시트마다 다르고 나중에 늘어날 수도 있어, 컬럼을 하나씩 두는 대신
 * 통째로 담는 편이 스키마를 덜 흔든다.
 */
final class EvaluationColumnMappingConverter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final TypeReference<Map<String, String>> MAP_TYPE =
            new TypeReference<>() {
            };

    private EvaluationColumnMappingConverter() {
    }

    static String toJson(EvaluationColumnMapping columnMapping) {
        if (columnMapping == null) {
            return null;
        }

        return OBJECT_MAPPER.writeValueAsString(columnMapping.asMap());
    }

    static EvaluationColumnMapping fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return EvaluationColumnMapping.from(
                    OBJECT_MAPPER.readValue(json, MAP_TYPE)
            );
        } catch (RuntimeException exception) {
            /*
             * 저장할 때 우리가 만든 형식이라 정상 흐름에서는 깨질 일이 없다.
             * 사람이 DB 를 직접 고쳤을 때 무슨 일인지 알 수 있도록 남긴다.
             */
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "저장된 컬럼 매핑 정보를 읽을 수 없습니다.",
                    exception
            );
        }
    }
}

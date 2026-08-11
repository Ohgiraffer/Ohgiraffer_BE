package com.ohgiraffer.evaluation.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 시트의 어느 컬럼이 우리의 어느 값인지에 대한 약속.
 *
 * <p>시트는 강사가 만들기 때문에 컬럼 이름을 우리가 정할 수 없다. 어떤 시트는 "수강생",
 * 어떤 시트는 "이름"이다. 그래서 연동할 때 한 번 짝지어 두고 그 뒤로는 이 값을 보고 읽는다.
 *
 * <p>의견만 선택이다. 나머지 넷이 없으면 평가로서 성립하지 않는다.
 *
 * @param traineeIdentifier 훈련생을 찾을 컬럼. 값은 이메일로 해석한다
 */
public record EvaluationColumnMapping(
        String traineeIdentifier,
        String evaluationType,
        String item,
        String score,
        String comment
) {

    public EvaluationColumnMapping {
        traineeIdentifier = requireColumn(traineeIdentifier, "훈련생 식별자");
        evaluationType = requireColumn(evaluationType, "평가 유형");
        item = requireColumn(item, "평가 항목");
        score = requireColumn(score, "점수");
        comment = trimToNull(comment);
    }

    /**
     * 짝지어 둔 컬럼이 시트에 실제로 있는지 확인한다.
     *
     * <p>저장할 때 한 번, 동기화할 때 한 번 확인한다. 저장 시점에 맞았더라도 그 뒤에
     * 시트에서 컬럼 이름을 바꾸면 어긋나기 때문이다.
     *
     * @param sheetColumns 시트에서 읽어온 실제 컬럼 목록
     */
    public void validateAgainst(List<String> sheetColumns) {
        for (Map.Entry<String, String> entry : asMap().entrySet()) {
            if (!sheetColumns.contains(entry.getValue())) {
                throw new BusinessException(
                        ErrorCode.EVALUATION_SHEET_COLUMN_NOT_FOUND,
                        "시트에 '" + entry.getValue() + "' 컬럼이 없습니다."
                                + " 사용할 수 있는 컬럼: "
                                + String.join(", ", sheetColumns)
                );
            }
        }
    }

    /**
     * 저장과 전달에 쓰는 형태. 값이 없는 의견은 빼고 담는다.
     *
     * <p>순서를 지키려고 LinkedHashMap 을 쓴다. JSON 으로 저장되므로 순서가 뒤바뀌면
     * 내용이 같아도 다른 문자열이 되어, 바뀐 것이 없는데 바뀐 것처럼 보인다.
     */
    public Map<String, String> asMap() {
        Map<String, String> mapping = new LinkedHashMap<>();

        mapping.put("traineeIdentifier", traineeIdentifier);
        mapping.put("evaluationType", evaluationType);
        mapping.put("item", item);
        mapping.put("score", score);

        if (comment != null) {
            mapping.put("comment", comment);
        }

        return mapping;
    }

    public static EvaluationColumnMapping from(Map<String, String> mapping) {
        if (mapping == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "컬럼 매핑 정보가 필요합니다."
            );
        }

        return new EvaluationColumnMapping(
                mapping.get("traineeIdentifier"),
                mapping.get("evaluationType"),
                mapping.get("item"),
                mapping.get("score"),
                mapping.get("comment")
        );
    }

    private static String requireColumn(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    label + " 컬럼을 지정해야 합니다."
            );
        }

        return value.trim();
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}

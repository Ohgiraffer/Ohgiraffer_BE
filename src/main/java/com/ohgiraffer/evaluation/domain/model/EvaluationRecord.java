package com.ohgiraffer.evaluation.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;

/**
 * 시트에서 가져온 평가 한 건. JPA와 무관한 순수 객체다.
 *
 * <p>{@code sheetRowKey} 가 이 도메인의 핵심이다. 같은 평가를 두 번 저장하지 않으려면
 * "무엇이 같은 평가인가" 를 정해야 하는데, 시트에는 그런 식별자가 없다.
 * 그래서 <b>이메일 · 평가유형 · 평가항목</b> 을 이어 붙여 만든다.
 *
 * <p>행 번호를 쓰지 않는 이유가 있다. 강사가 시트를 이름순으로 정렬하거나 중간에 행을
 * 하나 끼워 넣으면 그 아래가 전부 밀려서, 같은 평가가 전혀 다른 것으로 보인다.
 * 위 세 값은 행이 어디로 움직이든 따라다닌다.
 */
public class EvaluationRecord {

    private static final int ITEM_MAX_LENGTH = 255;
    private static final int EVALUATION_TYPE_MAX_LENGTH = 50;
    private static final int SHEET_ROW_KEY_MAX_LENGTH = 255;

    private final Long id;
    private final Long traineeId;
    private final Long sheetLinkId;
    private final String evaluationType;
    private final String item;
    private final BigDecimal score;
    private final String comment;
    private final String sheetRowKey;
    private final Instant syncedAt;

    private EvaluationRecord(
            Long id,
            Long traineeId,
            Long sheetLinkId,
            String evaluationType,
            String item,
            BigDecimal score,
            String comment,
            String sheetRowKey,
            Instant syncedAt
    ) {
        this.id = id;
        this.traineeId = traineeId;
        this.sheetLinkId = sheetLinkId;
        this.evaluationType = evaluationType;
        this.item = item;
        this.score = score;
        this.comment = comment;
        this.sheetRowKey = sheetRowKey;
        this.syncedAt = syncedAt;
    }

    public static EvaluationRecord create(
            Long traineeId,
            Long sheetLinkId,
            String traineeEmail,
            String evaluationType,
            String item,
            BigDecimal score,
            String comment
    ) {
        validateTraineeId(traineeId);
        validateSheetLinkId(sheetLinkId);

        String normalizedType = requireText(evaluationType, "평가 유형",
                EVALUATION_TYPE_MAX_LENGTH);
        String normalizedItem = requireText(item, "평가 항목", ITEM_MAX_LENGTH);

        return new EvaluationRecord(
                null,
                traineeId,
                sheetLinkId,
                normalizedType,
                normalizedItem,
                score,
                trimToNull(comment),
                sheetRowKey(traineeEmail, normalizedType, normalizedItem),
                Instant.now()
        );
    }

    /**
     * 저장소에서 읽어온 값으로 복원한다. 검증을 다시 수행하지 않는다.
     */
    public static EvaluationRecord restore(
            Long id,
            Long traineeId,
            Long sheetLinkId,
            String evaluationType,
            String item,
            BigDecimal score,
            String comment,
            String sheetRowKey,
            Instant syncedAt
    ) {
        return new EvaluationRecord(
                id, traineeId, sheetLinkId, evaluationType, item,
                score, comment, sheetRowKey, syncedAt
        );
    }

    /**
     * 시트 행을 가리키는 값. 이메일은 대소문자를 가리지 않는다.
     *
     * <p>구분자로 세로줄을 쓴다. 이메일·평가유형·항목 어디에도 잘 나오지 않는 글자라
     * 값 안에 섞여 경계가 흐려질 일이 적다.
     */
    public static String sheetRowKey(
            String traineeEmail,
            String evaluationType,
            String item
    ) {
        String key = traineeEmail.trim().toLowerCase(Locale.ROOT)
                + "|" + evaluationType.trim()
                + "|" + item.trim();

        if (key.length() > SHEET_ROW_KEY_MAX_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "평가 식별값이 너무 깁니다: " + key
            );
        }

        return key;
    }

    /**
     * 시트에서 값이 바뀌었는지 여부.
     *
     * <p>식별자와 시각은 보지 않는다. 같은 평가인지는 {@code sheetRowKey} 가 이미 정했고,
     * 여기서는 그 평가의 내용이 달라졌는지만 본다.
     *
     * <p>점수는 {@code compareTo} 로 비교한다. {@code equals} 를 쓰면 85 와 85.00 이
     * 다른 값으로 잡혀, 바뀐 것이 없는데 매번 수정으로 기록된다.
     */
    public boolean differsFrom(EvaluationRecord other) {
        return !sameScore(other.score)
                || !java.util.Objects.equals(comment, other.comment);
    }

    private boolean sameScore(BigDecimal other) {
        if (score == null || other == null) {
            return score == other;
        }

        return score.compareTo(other) == 0;
    }

    /**
     * 기존 행에 새로 읽어온 값을 덮어쓴다. 식별자와 키는 그대로 둔다.
     */
    public EvaluationRecord updateFrom(EvaluationRecord source) {
        return new EvaluationRecord(
                id,
                traineeId,
                sheetLinkId,
                source.evaluationType,
                source.item,
                source.score,
                source.comment,
                sheetRowKey,
                Instant.now()
        );
    }

    private static void validateTraineeId(Long traineeId) {
        if (traineeId == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "훈련생 정보가 필요합니다."
            );
        }
    }

    private static void validateSheetLinkId(Long sheetLinkId) {
        if (sheetLinkId == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "시트 연동 정보가 필요합니다."
            );
        }
    }

    private static String requireText(String value, String label, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    label + " 값이 비어 있습니다."
            );
        }

        String trimmed = value.trim();

        if (trimmed.length() > maxLength) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    label + " 값이 너무 깁니다: " + trimmed
            );
        }

        return trimmed;
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    public Long getId() {
        return id;
    }

    public Long getTraineeId() {
        return traineeId;
    }

    public Long getSheetLinkId() {
        return sheetLinkId;
    }

    public String getEvaluationType() {
        return evaluationType;
    }

    public String getItem() {
        return item;
    }

    public BigDecimal getScore() {
        return score;
    }

    public String getComment() {
        return comment;
    }

    public String getSheetRowKey() {
        return sheetRowKey;
    }

    public Instant getSyncedAt() {
        return syncedAt;
    }
}

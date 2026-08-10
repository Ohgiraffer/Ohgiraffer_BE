package com.ohgiraffer.evaluation.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.time.Instant;

/**
 * 평가 데이터를 가져올 구글 시트 연동 정보. JPA와 무관한 순수 객체다.
 *
 * <p>{@code external_sheet_link} 테이블은 예산(be1)과 함께 쓴다. {@code domain} 값으로 나뉘고
 * 그 컬럼에 유니크 제약이 있어 도메인당 한 줄만 존재한다. 그래서 연동을 다시 저장하는 것은
 * 새로 만드는 것이 아니라 기존 줄을 덮어쓰는 일이다.
 *
 * <p>{@code lastSyncedAt} 은 여기서 바꾸지 않는다. 동기화가 끝난 뒤에 채워지는 값이고,
 * 연동 정보를 고친다고 해서 동기화한 시점이 달라지지는 않는다.
 */
public class EvaluationSheetLink {

    /** {@code external_sheet_link.domain} 에 들어가는 값. 예산 쪽 행과 구분한다. */
    public static final String DOMAIN = "EVALUATION";

    private static final int SHEET_URL_MAX_LENGTH = 500;
    private static final int TAB_NAME_MAX_LENGTH = 100;

    private final Long id;
    private final String sheetUrl;
    private final String tabName;
    private final EvaluationColumnMapping columnMapping;
    private final Instant lastSyncedAt;

    private EvaluationSheetLink(
            Long id,
            String sheetUrl,
            String tabName,
            EvaluationColumnMapping columnMapping,
            Instant lastSyncedAt
    ) {
        this.id = id;
        this.sheetUrl = sheetUrl;
        this.tabName = tabName;
        this.columnMapping = columnMapping;
        this.lastSyncedAt = lastSyncedAt;
    }

    public static EvaluationSheetLink create(
            String sheetUrl,
            String tabName,
            EvaluationColumnMapping columnMapping
    ) {
        validateSheetUrl(sheetUrl);
        validateTabName(tabName);
        validateColumnMapping(columnMapping);

        return new EvaluationSheetLink(
                null,
                sheetUrl.trim(),
                tabName.trim(),
                columnMapping,
                null
        );
    }

    /**
     * 저장소에서 읽어온 값으로 복원한다. 검증을 다시 수행하지 않는다.
     */
    public static EvaluationSheetLink restore(
            Long id,
            String sheetUrl,
            String tabName,
            EvaluationColumnMapping columnMapping,
            Instant lastSyncedAt
    ) {
        return new EvaluationSheetLink(
                id,
                sheetUrl,
                tabName,
                columnMapping,
                lastSyncedAt
        );
    }

    /**
     * 새 내용으로 바꾼 연동 정보를 만든다.
     *
     * <p>식별자와 마지막 동기화 시각은 그대로 옮긴다. 유니크 제약 때문에 같은 줄을
     * 덮어써야 하고, 설정을 고친 것이 동기화를 한 것은 아니기 때문이다.
     */
    public EvaluationSheetLink update(
            String sheetUrl,
            String tabName,
            EvaluationColumnMapping columnMapping
    ) {
        validateSheetUrl(sheetUrl);
        validateTabName(tabName);
        validateColumnMapping(columnMapping);

        return new EvaluationSheetLink(
                id,
                sheetUrl.trim(),
                tabName.trim(),
                columnMapping,
                lastSyncedAt
        );
    }

    /**
     * 동기화를 마친 시각을 기록한 연동 정보를 만든다.
     *
     * <p>연동 설정을 고치는 것과 나누어 둔다. 설정을 바꿨다고 동기화를 한 것은 아니고,
     * 동기화했다고 설정이 달라지지도 않는다.
     */
    public EvaluationSheetLink markSynced(Instant syncedAt) {
        return new EvaluationSheetLink(
                id,
                sheetUrl,
                tabName,
                columnMapping,
                syncedAt
        );
    }

    private static void validateSheetUrl(String sheetUrl) {
        if (sheetUrl == null || sheetUrl.isBlank()) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_SHEET_INVALID_URL,
                    "스프레드시트 주소가 필요합니다."
            );
        }

        if (sheetUrl.trim().length() > SHEET_URL_MAX_LENGTH) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_SHEET_INVALID_URL,
                    "스프레드시트 주소가 너무 깁니다."
            );
        }
    }

    private static void validateTabName(String tabName) {
        if (tabName == null || tabName.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "읽어올 시트 탭이 필요합니다."
            );
        }

        if (tabName.trim().length() > TAB_NAME_MAX_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "시트 탭 이름이 너무 깁니다."
            );
        }
    }

    private static void validateColumnMapping(
            EvaluationColumnMapping columnMapping
    ) {
        if (columnMapping == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "컬럼 매핑 정보가 필요합니다."
            );
        }
    }

    public Long getId() {
        return id;
    }

    public String getSheetUrl() {
        return sheetUrl;
    }

    public String getTabName() {
        return tabName;
    }

    public EvaluationColumnMapping getColumnMapping() {
        return columnMapping;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }
}

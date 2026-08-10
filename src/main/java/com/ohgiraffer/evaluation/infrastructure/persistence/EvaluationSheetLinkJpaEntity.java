package com.ohgiraffer.evaluation.infrastructure.persistence;

import com.ohgiraffer.evaluation.domain.model.EvaluationColumnMapping;
import com.ohgiraffer.evaluation.domain.model.EvaluationSheetLink;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * 평가 시트 연동 정보.
 *
 * <p>{@code external_sheet_link} 는 예산(be1)과 함께 쓰는 테이블이다. 그쪽에도 같은 테이블을
 * 가리키는 엔티티가 있지만 {@code domain} 값으로 행이 나뉘어 서로 건드리지 않는다.
 * 남의 패키지 엔티티를 가져다 쓰면 도메인 경계가 무너지므로 각자 둔다.
 *
 * <p>{@code domain} 은 항상 {@code EVALUATION} 이라 도메인 모델에는 두지 않는다.
 * 저장할 때 여기서 채우고 읽을 때 조건으로만 쓴다.
 *
 * <p>컬럼 매핑은 DB 에 JSON 문자열로 들어간다. 변환은
 * {@link EvaluationColumnMappingConverter} 가 맡는다.
 */
@Entity
@Table(name = "external_sheet_link")
public class EvaluationSheetLinkJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sheet_link_id")
    private Long id;

    @Column(name = "domain", nullable = false, length = 30)
    private String domain;

    @Column(name = "sheet_url", nullable = false, length = 500)
    private String sheetUrl;

    @Column(name = "tab_name", length = 100)
    private String tabName;

    @Column(name = "column_mapping", columnDefinition = "JSON")
    private String columnMapping;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    protected EvaluationSheetLinkJpaEntity() {
    }

    private EvaluationSheetLinkJpaEntity(EvaluationSheetLink sheetLink) {
        this.id = sheetLink.getId();
        this.domain = EvaluationSheetLink.DOMAIN;
        this.sheetUrl = sheetLink.getSheetUrl();
        this.tabName = sheetLink.getTabName();
        this.columnMapping = EvaluationColumnMappingConverter
                .toJson(sheetLink.getColumnMapping());
        this.lastSyncedAt = sheetLink.getLastSyncedAt();
    }

    public static EvaluationSheetLinkJpaEntity from(
            EvaluationSheetLink sheetLink
    ) {
        return new EvaluationSheetLinkJpaEntity(sheetLink);
    }

    public EvaluationSheetLink toDomain() {
        return EvaluationSheetLink.restore(
                id,
                sheetUrl,
                tabName,
                EvaluationColumnMappingConverter.fromJson(columnMapping),
                lastSyncedAt
        );
    }
}

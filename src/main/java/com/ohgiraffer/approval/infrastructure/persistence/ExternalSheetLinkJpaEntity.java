package com.ohgiraffer.approval.infrastructure.persistence;

import com.ohgiraffer.approval.domain.model.budget.ExternalSheetLink;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "external_sheet_link")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExternalSheetLinkJpaEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "sheet_link_id")
    private Long id;

    @Column(
            name = "domain",
            nullable = false,
            length = 20
    )
    private String domain;

    @Column(
            name = "sheet_url",
            nullable = false,
            length = 500
    )
    private String sheetUrl;

    @Column(
            name = "tab_name",
            length = 100
    )
    private String tabName;

    @Column(
            name = "column_mapping",
            columnDefinition = "JSON"
    )
    private String columnMapping;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    private ExternalSheetLinkJpaEntity(
            Long id,
            String domain,
            String sheetUrl,
            String tabName,
            String columnMapping,
            LocalDateTime lastSyncedAt
    ) {
        this.id = id;
        this.domain = domain;
        this.sheetUrl = sheetUrl;
        this.tabName = tabName;
        this.columnMapping = columnMapping;
        this.lastSyncedAt = lastSyncedAt;
    }

    public static ExternalSheetLinkJpaEntity from(
            ExternalSheetLink externalSheetLink
    ) {
        return new ExternalSheetLinkJpaEntity(
                externalSheetLink.getId(),
                externalSheetLink.getDomain(),
                externalSheetLink.getSheetUrl(),
                externalSheetLink.getTabName(),
                externalSheetLink.getColumnMapping(),
                externalSheetLink.getLastSyncedAt()
        );
    }

    public ExternalSheetLink toDomain() {
        return ExternalSheetLink.restore(
                id,
                domain,
                sheetUrl,
                tabName,
                columnMapping,
                lastSyncedAt
        );
    }
}
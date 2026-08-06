package com.ohgiraffer.approval.domain.model.budget;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ExternalSheetLink {

    private static final String BUDGET_DOMAIN = "BUDGET";

    private final Long id;
    private final String domain;
    private String sheetUrl;
    private String tabName;
    private String columnMapping;
    private LocalDateTime lastSyncedAt;

    public static ExternalSheetLink createBudgetLink(
            String sheetUrl,
            String tabName,
            String columnMapping,
            LocalDateTime lastSyncedAt
    ) {
        ExternalSheetLink externalSheetLink =
                new ExternalSheetLink(
                        null,
                        BUDGET_DOMAIN
                );

        externalSheetLink.update(
                sheetUrl,
                tabName,
                columnMapping,
                lastSyncedAt
        );

        return externalSheetLink;
    }

    public static ExternalSheetLink restore(
            Long id,
            String domain,
            String sheetUrl,
            String tabName,
            String columnMapping,
            LocalDateTime lastSyncedAt
    ) {
        ExternalSheetLink externalSheetLink =
                new ExternalSheetLink(
                        id,
                        domain
                );

        externalSheetLink.sheetUrl = sheetUrl;
        externalSheetLink.tabName = tabName;
        externalSheetLink.columnMapping = columnMapping;
        externalSheetLink.lastSyncedAt = lastSyncedAt;

        return externalSheetLink;
    }

    public void update(
            String sheetUrl,
            String tabName,
            String columnMapping,
            LocalDateTime lastSyncedAt
    ) {
        this.sheetUrl = sheetUrl;
        this.tabName = tabName;
        this.columnMapping = columnMapping;
        this.lastSyncedAt = lastSyncedAt;
    }

    public static String budgetDomain() {
        return BUDGET_DOMAIN;
    }
}
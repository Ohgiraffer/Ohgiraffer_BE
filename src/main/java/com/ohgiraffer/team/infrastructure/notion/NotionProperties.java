package com.ohgiraffer.team.infrastructure.notion;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notion")
public record NotionProperties(
        String apiToken,
        String dataSourceId,
        String templateId,
        String apiVersion
) {

    public NotionProperties {
        validateRequiredValue(apiToken, "Notion API token");
        validateRequiredValue(dataSourceId, "Notion data source ID");
        validateRequiredValue(templateId, "Notion template ID");

        if (apiVersion == null || apiVersion.isBlank()) {
            apiVersion = "2026-03-11";
        }

        apiToken = apiToken.trim();
        dataSourceId = dataSourceId.trim();
        templateId = templateId.trim();
        apiVersion = apiVersion.trim();
    }

    private static void validateRequiredValue(
            String value,
            String name
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    name + "가 설정되지 않았습니다."
            );
        }
    }
}
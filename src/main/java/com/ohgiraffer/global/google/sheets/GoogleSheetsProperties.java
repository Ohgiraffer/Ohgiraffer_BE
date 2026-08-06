package com.ohgiraffer.global.google.sheets;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google.sheets")
public record GoogleSheetsProperties(
        boolean enabled,
        String applicationName,
        String credentialsLocation
) {
}
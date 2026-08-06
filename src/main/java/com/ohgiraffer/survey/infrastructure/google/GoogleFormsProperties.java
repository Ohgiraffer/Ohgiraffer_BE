package com.ohgiraffer.survey.infrastructure.google;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google.forms")
public record GoogleFormsProperties(
        boolean enabled,
        String applicationName,
        String oauthCredentialsLocation,
        String tokenDirectory,
        String credentialUserKey
) {
}
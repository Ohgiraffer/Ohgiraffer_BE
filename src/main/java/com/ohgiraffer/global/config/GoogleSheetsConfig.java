package com.ohgiraffer.global.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.ohgiraffer.global.google.sheets.GoogleSheetsClient;
import com.ohgiraffer.global.google.sheets.GoogleSheetsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GoogleSheetsProperties.class)
@ConditionalOnProperty(
        prefix = "google.sheets",
        name = "enabled",
        havingValue = "true"
)
public class GoogleSheetsConfig {

    @Bean
    public Sheets googleSheets(
            GoogleSheetsProperties properties,
            ResourceLoader resourceLoader
    ) throws IOException, GeneralSecurityException {

        validateProperties(properties);

        Resource credentialsResource =
                resourceLoader.getResource(
                        properties.credentialsLocation()
                );

        if (!credentialsResource.exists()) {
            throw new IllegalStateException(
                    "Google 서비스 계정 키 파일을 찾을 수 없습니다: "
                            + properties.credentialsLocation()
            );
        }

        GoogleCredentials credentials;

        try (InputStream inputStream =
                     credentialsResource.getInputStream()) {

            credentials = GoogleCredentials
                    .fromStream(inputStream)
                    .createScoped(
                            List.of(
                                    SheetsScopes.SPREADSHEETS_READONLY
                            )
                    );
        }

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        )
                .setApplicationName(properties.applicationName())
                .build();
    }

    @Bean
    public GoogleSheetsClient googleSheetsClient(
            Sheets googleSheets
    ) {
        return new GoogleSheetsClient(googleSheets);
    }

    private void validateProperties(
            GoogleSheetsProperties properties
    ) {
        if (properties.credentialsLocation() == null
                || properties.credentialsLocation().isBlank()) {
            throw new IllegalStateException(
                    "GOOGLE_SHEETS_CREDENTIALS_LOCATION 환경변수가 필요합니다."
            );
        }

        if (properties.applicationName() == null
                || properties.applicationName().isBlank()) {
            throw new IllegalStateException(
                    "Google Sheets application-name 설정이 필요합니다."
            );
        }
    }
}
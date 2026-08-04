package com.ohgiraffer.survey.infrastructure.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.forms.v1.Forms;
import com.google.api.services.forms.v1.FormsScopes;
import com.ohgiraffer.survey.application.port.GoogleFormPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.context.annotation.Primary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GoogleFormsProperties.class)
@ConditionalOnProperty(
        prefix = "google.forms",
        name = "enabled",
        havingValue = "true"
)
public class GoogleFormsConfig {

    private static final JsonFactory JSON_FACTORY =
            GsonFactory.getDefaultInstance();

    private static final List<String> SCOPES =
            List.of(
                    FormsScopes.FORMS_BODY,
                    DriveScopes.DRIVE_FILE
            );

    @Bean(name = "googleFormsCredential")
    public Credential googleFormsCredential(
            GoogleFormsProperties properties,
            ResourceLoader resourceLoader
    ) throws IOException, GeneralSecurityException {

        validateProperties(properties);

        Resource oauthResource =
                resolveResource(
                        properties.oauthCredentialsLocation(),
                        resourceLoader
                );

        if (!oauthResource.exists()) {
            throw new IllegalStateException(
                    "Google Forms OAuth 클라이언트 파일을 찾을 수 없습니다: "
                            + properties.oauthCredentialsLocation()
            );
        }

        GoogleClientSecrets clientSecrets;

        try (InputStream inputStream =
                     oauthResource.getInputStream();
             InputStreamReader reader =
                     new InputStreamReader(
                             inputStream,
                             StandardCharsets.UTF_8
                     )) {

            clientSecrets =
                    GoogleClientSecrets.load(
                            JSON_FACTORY,
                            reader
                    );
        }

        if (clientSecrets.getInstalled() == null) {
            throw new IllegalStateException(
                    "Google Forms OAuth 파일은 "
                            + "데스크톱 앱 클라이언트 JSON이어야 합니다."
            );
        }

        File tokenDirectory =
                new File(
                        properties.tokenDirectory().trim()
                );

        if (!tokenDirectory.exists()
                || !tokenDirectory.isDirectory()) {
            throw new IllegalStateException(
                    "Google Forms 토큰 폴더를 찾을 수 없습니다: "
                            + properties.tokenDirectory()
            );
        }

        GoogleAuthorizationCodeFlow authorizationFlow =
                new GoogleAuthorizationCodeFlow.Builder(
                        GoogleNetHttpTransport
                                .newTrustedTransport(),
                        JSON_FACTORY,
                        clientSecrets,
                        SCOPES
                )
                        .setDataStoreFactory(
                                new FileDataStoreFactory(
                                        tokenDirectory
                                )
                        )
                        .setAccessType("offline")
                        .build();

        /*
         * 여기서는 브라우저 로그인을 실행하지 않습니다.
         *
         * GoogleFormsConnectionTest에서 발급받아 저장한
         * 기존 OAuth 토큰을 읽기만 합니다.
         */
        Credential credential =
                authorizationFlow.loadCredential(
                        properties
                                .credentialUserKey()
                                .trim()
                );

        if (credential == null) {
            throw new IllegalStateException(
                    "저장된 Google Forms OAuth 토큰을 찾을 수 없습니다. "
                            + "GoogleFormsConnectionTest를 먼저 실행해주세요."
            );
        }

        if (credential.getRefreshToken() == null
                || credential.getRefreshToken().isBlank()) {
            throw new IllegalStateException(
                    "Google Forms OAuth refresh token이 없습니다. "
                            + "forms-tokens 폴더를 비운 뒤 "
                            + "GoogleFormsConnectionTest를 다시 실행해주세요."
            );
        }

        return credential;
    }

    @Bean
    public Forms googleForms(
            GoogleFormsProperties properties,
            @Qualifier("googleFormsCredential")
            Credential credential
    ) throws GeneralSecurityException, IOException {

        return new Forms.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential
        )
                .setApplicationName(
                        properties.applicationName()
                )
                .build();
    }

    @Bean
    public Drive googleDriveForForms(
            GoogleFormsProperties properties,
            @Qualifier("googleFormsCredential")
            Credential credential
    ) throws GeneralSecurityException, IOException {

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential
        )
                .setApplicationName(
                        properties.applicationName()
                )
                .build();
    }

    @Bean
    @Primary
    public GoogleFormPort googleFormPort(
            Forms googleForms,
            Drive googleDriveForForms
    ) {
        return new GoogleFormsAdapter(
                googleForms,
                googleDriveForForms
        );
    }

    private void validateProperties(
            GoogleFormsProperties properties
    ) {
        if (properties.applicationName() == null
                || properties.applicationName().isBlank()) {
            throw new IllegalStateException(
                    "google.forms.application-name 설정이 필요합니다."
            );
        }

        if (properties.oauthCredentialsLocation() == null
                || properties.oauthCredentialsLocation().isBlank()) {
            throw new IllegalStateException(
                    "GOOGLE_FORMS_OAUTH_CREDENTIALS_LOCATION "
                            + "환경변수가 필요합니다."
            );
        }

        if (properties.tokenDirectory() == null
                || properties.tokenDirectory().isBlank()) {
            throw new IllegalStateException(
                    "GOOGLE_FORMS_TOKEN_DIRECTORY "
                            + "환경변수가 필요합니다."
            );
        }

        if (properties.credentialUserKey() == null
                || properties.credentialUserKey().isBlank()) {
            throw new IllegalStateException(
                    "google.forms.credential-user-key "
                            + "설정이 필요합니다."
            );
        }
    }

    private Resource resolveResource(
            String location,
            ResourceLoader resourceLoader
    ) {
        String trimmedLocation =
                location.trim();

        if (trimmedLocation.startsWith("classpath:")
                || trimmedLocation.startsWith("file:")) {
            return resourceLoader.getResource(
                    trimmedLocation
            );
        }

        return new FileSystemResource(
                trimmedLocation
        );
    }
}
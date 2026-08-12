package com.ohgiraffer.global.google.forms;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.forms.v1.Forms;
import com.google.api.services.forms.v1.FormsScopes;
import com.google.api.services.forms.v1.model.Form;
import com.google.api.services.forms.v1.model.Info;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class GoogleFormsConnectionTest {

    private static final String APPLICATION_NAME =
            "ohgiraffer-forms-connection-test";

    private static final String TEST_FORM_TITLE =
            "[TEST] Ohgiraffer Google Forms 연동 테스트";

    private static final JsonFactory JSON_FACTORY =
            GsonFactory.getDefaultInstance();

    private static final List<String> SCOPES =
            List.of(
                    FormsScopes.FORMS_BODY,
                    FormsScopes.FORMS_RESPONSES_READONLY,
                    DriveScopes.DRIVE_FILE
            );

    @Test
    void 사용자_OAuth로_빈_구글폼을_생성한다(
            TestReporter testReporter
    ) throws Exception {

        /*
         * 1. 실행 환경변수를 읽습니다.
         */
        String oauthCredentialsLocation =
                System.getenv(
                        "GOOGLE_FORMS_OAUTH_CREDENTIALS_LOCATION"
                );

        String tokenDirectory =
                System.getenv(
                        "GOOGLE_FORMS_TOKEN_DIRECTORY"
                );

        /*
         * 환경변수가 없는 CI에서는 통합 테스트만 건너뜁니다.
         */
        assumeTrue(
                oauthCredentialsLocation != null
                        && !oauthCredentialsLocation.isBlank(),
                "GOOGLE_FORMS_OAUTH_CREDENTIALS_LOCATION이 없어 "
                        + "Google Forms 연동 테스트를 건너뜁니다."
        );

        assumeTrue(
                tokenDirectory != null
                        && !tokenDirectory.isBlank(),
                "GOOGLE_FORMS_TOKEN_DIRECTORY가 없어 "
                        + "Google Forms 연동 테스트를 건너뜁니다."
        );

        /*
         * 2. OAuth 클라이언트 JSON 파일을 찾습니다.
         *
         * file:C:/... 형태를 지원하기 위해 ResourceLoader를 사용합니다.
         */
        Resource oauthResource =
                new DefaultResourceLoader()
                        .getResource(
                                oauthCredentialsLocation.trim()
                        );

        assertNotNull(
                oauthResource,
                "OAuth 파일 리소스가 null입니다."
        );

        assertFalse(
                !oauthResource.exists(),
                "OAuth 클라이언트 JSON 파일을 찾을 수 없습니다: "
                        + oauthCredentialsLocation
        );

        /*
         * 3. Google Cloud에서 받은 데스크톱 OAuth 정보를 읽습니다.
         */
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

        assertNotNull(
                clientSecrets,
                "OAuth 클라이언트 JSON을 읽지 못했습니다."
        );

        assertNotNull(
                clientSecrets.getInstalled(),
                "데스크톱 앱 OAuth JSON이 아닙니다. "
                        + "JSON 최상위에 installed가 있어야 합니다."
        );

        /*
         * 4. Google 사용자 OAuth 인증 흐름을 생성합니다.
         *
         * 로그인 성공 후 토큰은 forms-tokens 폴더에 저장됩니다.
         */
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
                                        new File(
                                                tokenDirectory.trim()
                                        )
                                )
                        )
                        .setAccessType("offline")
                        .build();

        /*
         * 5. Google 로그인 완료 후 localhost:8888로 돌아옵니다.
         */
        LocalServerReceiver receiver =
                new LocalServerReceiver.Builder()
                        .setHost("localhost")
                        .setPort(8888)
                        .build();

        /*
         * 6. 최초 실행 시 브라우저에서 Google 로그인 및 동의를 진행합니다.
         */
        Credential credential =
                new AuthorizationCodeInstalledApp(
                        authorizationFlow,
                        receiver
                )
                        .authorize(
                                "ohgiraffer-forms-owner"
                        );

        assertNotNull(
                credential,
                "Google OAuth 인증 정보가 생성되지 않았습니다."
        );

        /*
         * 7. 인증된 사용자로 Google Forms API 클라이언트를 만듭니다.
         */
        Forms forms =
                new Forms.Builder(
                        GoogleNetHttpTransport
                                .newTrustedTransport(),
                        JSON_FACTORY,
                        credential
                )
                        .setApplicationName(
                                APPLICATION_NAME
                        )
                        .build();

        assertNotNull(
                forms,
                "Google Forms API 클라이언트가 생성되지 않았습니다."
        );

        /*
         * 8. 제목만 들어 있는 빈 Form을 만듭니다.
         */
        Form requestedForm =
                new Form()
                        .setInfo(
                                new Info()
                                        .setTitle(
                                                TEST_FORM_TITLE
                                        )
                                        .setDocumentTitle(
                                                TEST_FORM_TITLE
                                        )
                        );

        /*
         * 9. unpublished=true로 초안 Form을 생성합니다.
         *
         * 아직 훈련생에게 공개되지 않고 응답도 받지 않습니다.
         */
        Form createdForm =
                forms
                        .forms()
                        .create(
                                requestedForm
                        )
                        .setUnpublished(true)
                        .execute();

        /*
         * 10. Google 응답을 검증합니다.
         */
        assertNotNull(
                createdForm,
                "Google Form 생성 결과가 null입니다."
        );

        assertNotNull(
                createdForm.getFormId(),
                "생성된 Google Form ID가 null입니다."
        );

        assertFalse(
                createdForm.getFormId().isBlank(),
                "생성된 Google Form ID가 비어 있습니다."
        );

        assertNotNull(
                createdForm.getInfo(),
                "생성된 Google Form 정보가 null입니다."
        );

        assertEquals(
                TEST_FORM_TITLE,
                createdForm.getInfo().getTitle(),
                "생성된 Google Form 제목이 요청한 제목과 다릅니다."
        );

        /*
         * 11. 프론트에 반환할 수 있는 편집 URL을 만듭니다.
         */
        String editUrl =
                "https://docs.google.com/forms/d/"
                        + createdForm.getFormId()
                        + "/edit";

        /*
         * System.out.println 대신 JUnit TestReporter로 결과를 표시합니다.
         */
        testReporter.publishEntry(
                "생성된 Form ID",
                createdForm.getFormId()
        );

        testReporter.publishEntry(
                "Google Form 편집 URL",
                editUrl
        );
    }
}
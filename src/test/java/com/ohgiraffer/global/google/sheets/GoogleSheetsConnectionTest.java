package com.ohgiraffer.global.google.sheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.ohgiraffer.global.config.GoogleSheetsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class GoogleSheetsConnectionTest {

    private static final String SPREADSHEET_URL =
            "https://docs.google.com/spreadsheets/d/"
                    + "1s3Gph7FUgkK7hSg5ED5TDzAfk7JWOXGAJwh-KXthP8Y"
                    + "/edit?usp=sharing";

    private static final String EXPECTED_SPREADSHEET_ID =
            "1s3Gph7FUgkK7hSg5ED5TDzAfk7JWOXGAJwh-KXthP8Y";

    @Test
    void 서비스_계정으로_스프레드시트를_조회한다()
            throws Exception {

        String credentialsLocation =
                System.getenv(
                        "GOOGLE_SHEETS_CREDENTIALS_LOCATION"
                );

        /*
         * 서비스 계정 환경변수가 없는 팀원 또는 CI 환경에서는
         * 실패시키지 않고 통합 테스트를 건너뜁니다.
         */
        assumeTrue(
                credentialsLocation != null
                        && !credentialsLocation.isBlank(),
                "Google Sheets 인증 환경변수가 없어 "
                        + "통합 테스트를 건너뜁니다."
        );

        GoogleSheetsProperties properties =
                new GoogleSheetsProperties(
                        true,
                        "ohgiraffer-test",
                        credentialsLocation
                );

        GoogleSheetsConfig config =
                new GoogleSheetsConfig();

        Sheets sheets = config.googleSheets(
                properties,
                new DefaultResourceLoader()
        );

        GoogleSheetsClient client =
                new GoogleSheetsClient(sheets);

        SpreadsheetIdExtractor extractor =
                new SpreadsheetIdExtractor();

        /*
         * 1. Google 스프레드시트 URL에서 ID 추출 확인
         */
        String spreadsheetId =
                extractor.extract(
                        SPREADSHEET_URL
                );

        assertEquals(
                EXPECTED_SPREADSHEET_ID,
                spreadsheetId
        );

        /*
         * 2. 서비스 계정으로 스프레드시트 제목 조회
         */
        String title =
                client.getSpreadsheetTitle(
                        spreadsheetId
                );

        assertNotNull(title);
        assertFalse(title.isBlank());

        System.out.println(
                "스프레드시트 제목: " + title
        );

        /*
         * 3. 스프레드시트의 탭 목록 조회
         */
        Spreadsheet spreadsheet = sheets
                .spreadsheets()
                .get(spreadsheetId)
                .setFields(
                        "sheets.properties.title"
                )
                .execute();

        List<Sheet> sheetList =
                spreadsheet.getSheets();

        assertNotNull(sheetList);

        assertFalse(
                sheetList.isEmpty(),
                "스프레드시트에 탭이 없습니다."
        );

        System.out.println("탭 목록:");

        for (Sheet sheet : sheetList) {
            String sheetTitle =
                    sheet
                            .getProperties()
                            .getTitle();

            System.out.println(
                    "- " + sheetTitle
            );
        }

        /*
         * 4. 첫 번째 탭 이름 가져오기
         *
         * 프로젝트가 Java 17이므로 getFirst()가 아니라
         * get(0)을 사용합니다.
         */
        String firstSheetTitle =
                sheetList
                        .get(0)
                        .getProperties()
                        .getTitle();

        /*
         * 탭 이름에 작은따옴표가 포함된 경우
         * Google Sheets 범위 문법에 맞게 처리합니다.
         */
        String escapedTitle =
                firstSheetTitle.replace(
                        "'",
                        "''"
                );

        String range =
                "'" + escapedTitle + "'!A1:C3";

        /*
         * 5. 실제 셀 데이터 조회
         */
        List<List<Object>> rows =
                client.readRange(
                        spreadsheetId,
                        range
                );

        assertNotNull(rows);

        assertFalse(
                rows.isEmpty(),
                "조회된 시트 데이터가 없습니다."
        );

        assertTrue(
                rows.size() >= 3,
                "테스트용 데이터가 3행 이상 필요합니다."
        );

        System.out.println(
                "조회 범위: " + range
        );

        System.out.println(
                "조회된 행 개수: " + rows.size()
        );

        for (List<Object> row : rows) {
            System.out.println(row);
        }

        /*
         * 6. 테스트 시트 내용 검증
         */
        assertEquals(
                List.of(
                        "이름",
                        "과정",
                        "상태"
                ),
                rows.get(0)
        );

        assertEquals(
                List.of(
                        "홍길동",
                        "백엔드",
                        "출석"
                ),
                rows.get(1)
        );

        assertEquals(
                List.of(
                        "김철수",
                        "백엔드",
                        "지각"
                ),
                rows.get(2)
        );
    }
}
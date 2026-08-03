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

        /*
         * 1. 테스트 실행 환경에서 서비스 계정 인증키 위치를 가져옵니다.
         */
        String credentialsLocation =
                System.getenv(
                        "GOOGLE_SHEETS_CREDENTIALS_LOCATION"
                );

        /*
         * 로컬 또는 CI 환경에 인증키 환경변수가 없다면
         * 테스트를 실패시키지 않고 건너뜁니다.
         */
        assumeTrue(
                credentialsLocation != null
                        && !credentialsLocation.isBlank(),
                "Google Sheets 인증 환경변수가 없어 "
                        + "통합 테스트를 건너뜁니다."
        );

        /*
         * 2. Google Sheets 설정 객체를 생성합니다.
         */
        GoogleSheetsProperties properties =
                new GoogleSheetsProperties(
                        true,
                        "ohgiraffer-test",
                        credentialsLocation
                );

        GoogleSheetsConfig config =
                new GoogleSheetsConfig();

        /*
         * 3. 서비스 계정 인증정보를 사용하여
         * Google Sheets API 객체를 생성합니다.
         */
        Sheets sheets = config.googleSheets(
                properties,
                new DefaultResourceLoader()
        );

        assertNotNull(
                sheets,
                "Google Sheets API 객체가 생성되지 않았습니다."
        );

        GoogleSheetsClient client =
                new GoogleSheetsClient(sheets);

        SpreadsheetIdExtractor extractor =
                new SpreadsheetIdExtractor();

        /*
         * 4. Google Sheets URL에서 스프레드시트 ID를 추출합니다.
         */
        String spreadsheetId =
                extractor.extract(
                        SPREADSHEET_URL
                );

        assertNotNull(
                spreadsheetId,
                "스프레드시트 ID가 추출되지 않았습니다."
        );

        assertFalse(
                spreadsheetId.isBlank(),
                "추출된 스프레드시트 ID가 비어 있습니다."
        );

        assertEquals(
                EXPECTED_SPREADSHEET_ID,
                spreadsheetId,
                "URL에서 추출한 스프레드시트 ID가 예상과 다릅니다."
        );

        /*
         * 5. 서비스 계정으로 스프레드시트 제목을 조회합니다.
         */
        String title =
                client.getSpreadsheetTitle(
                        spreadsheetId
                );

        assertNotNull(
                title,
                "스프레드시트 제목이 null입니다."
        );

        assertFalse(
                title.isBlank(),
                "스프레드시트 제목이 비어 있습니다."
        );

        /*
         * 6. 스프레드시트의 탭 목록을 조회합니다.
         */
        Spreadsheet spreadsheet = sheets
                .spreadsheets()
                .get(spreadsheetId)
                .setFields(
                        "sheets.properties.title"
                )
                .execute();

        assertNotNull(
                spreadsheet,
                "스프레드시트 조회 결과가 null입니다."
        );

        List<Sheet> sheetList =
                spreadsheet.getSheets();

        assertNotNull(
                sheetList,
                "스프레드시트 탭 목록이 null입니다."
        );

        assertFalse(
                sheetList.isEmpty(),
                "스프레드시트에 조회할 수 있는 탭이 없습니다."
        );

        /*
         * 7. 첫 번째 탭의 이름을 가져옵니다.
         *
         * 현재 프로젝트가 Java 17이므로
         * List.getFirst() 대신 get(0)을 사용합니다.
         */
        Sheet firstSheet =
                sheetList.get(0);

        assertNotNull(
                firstSheet,
                "첫 번째 시트가 null입니다."
        );

        assertNotNull(
                firstSheet.getProperties(),
                "첫 번째 시트의 properties가 null입니다."
        );

        String firstSheetTitle =
                firstSheet
                        .getProperties()
                        .getTitle();

        assertNotNull(
                firstSheetTitle,
                "첫 번째 시트의 이름이 null입니다."
        );

        assertFalse(
                firstSheetTitle.isBlank(),
                "첫 번째 시트의 이름이 비어 있습니다."
        );

        /*
         * 시트 이름에 작은따옴표가 들어 있다면
         * Google Sheets A1 표기법에 맞게 이스케이프합니다.
         */
        String escapedTitle =
                firstSheetTitle.replace(
                        "'",
                        "''"
                );

        String range =
                "'" + escapedTitle + "'!A1:C3";

        /*
         * 8. 첫 번째 탭의 A1:C3 범위를 조회합니다.
         */
        List<List<Object>> rows =
                client.readRange(
                        spreadsheetId,
                        range
                );

        assertNotNull(
                rows,
                "Google Sheets 범위 조회 결과가 null입니다."
        );

        assertFalse(
                rows.isEmpty(),
                "조회된 시트 데이터가 없습니다."
        );

        /*
         * 외부 Google Sheet의 실제 셀 값은 변경될 수 있으므로
         * 홍길동, 백엔드, 출석 등의 값은 직접 비교하지 않습니다.
         *
         * 이 테스트에서는 API 연결과 데이터 조회 여부만 확인합니다.
         */
        List<Object> firstRow =
                rows.get(0);

        assertNotNull(
                firstRow,
                "조회된 첫 번째 행이 null입니다."
        );

        assertFalse(
                firstRow.isEmpty(),
                "조회된 첫 번째 행에 셀 데이터가 없습니다."
        );
    }
}

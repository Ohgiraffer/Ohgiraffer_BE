package com.ohgiraffer.user;

import com.google.api.services.sheets.v4.Sheets;
import com.ohgiraffer.global.config.GoogleSheetsConfig;
import com.ohgiraffer.global.google.sheets.GoogleSheetsClient;
import com.ohgiraffer.global.google.sheets.GoogleSheetsProperties;
import com.ohgiraffer.global.google.sheets.SpreadsheetIdExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class StudentSheetConnectionTest {

    private static final String STUDENT_SHEET_URL =
            "https://docs.google.com/spreadsheets/d/1_0N5mcWykajc7bagHi4NoNVqiYwvp97di_QxiWGOtRQ/edit?gid=0#gid=0";

    /**
     * 조회할 범위. 탭 이름이 다르면 여기도 맞춰서 수정하세요.
     * 탭 이름에 공백이 있으면 작은따옴표로 감싸야 합니다. 예: '학생 명단'!A1:C50
     */
    private static final String RANGE = "시트1!A1:C50";

    @Test
    void 학생명단_시트를_읽고_행_데이터_형태를_확인한다() throws Exception {

        /*
         * 1. 인증 환경변수 확인. 없으면 테스트를 건너뜁니다.
         */
        String credentialsLocation =
                System.getenv("GOOGLE_SHEETS_CREDENTIALS_LOCATION");

        assumeTrue(
                credentialsLocation != null && !credentialsLocation.isBlank(),
                "Google Sheets 인증 환경변수가 없어 통합 테스트를 건너뜁니다."
        );

        /*
         * 2. Google Sheets 클라이언트 준비
         */
        GoogleSheetsProperties properties =
                new GoogleSheetsProperties(true, "ohgiraffer-test", credentialsLocation);

        Sheets sheets = new GoogleSheetsConfig()
                .googleSheets(properties, new DefaultResourceLoader());

        assertNotNull(sheets, "Google Sheets API 객체가 생성되지 않았습니다.");

        GoogleSheetsClient client = new GoogleSheetsClient(sheets);
        SpreadsheetIdExtractor extractor = new SpreadsheetIdExtractor();

        /*
         * 3. URL에서 Spreadsheet ID 추출
         */
        String spreadsheetId = extractor.extract(STUDENT_SHEET_URL);

        assertNotNull(spreadsheetId, "스프레드시트 ID가 추출되지 않았습니다.");
        assertFalse(spreadsheetId.isBlank(), "추출된 스프레드시트 ID가 비어 있습니다.");

        System.out.println("추출된 spreadsheetId = " + spreadsheetId);

        /*
         * 4. 시트 제목 조회 → 서비스 계정 공유 여부 확인
         *    여기서 403이 나면 공유 설정을 다시 확인해야 합니다.
         */
        String title = client.getSpreadsheetTitle(spreadsheetId);

        assertNotNull(title, "스프레드시트 제목이 null입니다.");
        assertFalse(title.isBlank(), "스프레드시트 제목이 비어 있습니다.");

        System.out.println("시트 제목 = " + title);

        /*
         * 5. 지정한 범위(A1:C50) 조회
         */
        List<List<Object>> rows = client.readRange(spreadsheetId, RANGE);

        assertNotNull(rows, "조회 결과가 null입니다.");
        assertFalse(rows.isEmpty(), "조회된 데이터가 없습니다. 헤더 행이라도 있는지 확인하세요.");

        System.out.println("총 조회된 행 개수 = " + rows.size());

        /*
         * 6. 헤더 행 확인
         */
        List<Object> header = rows.get(0);
        System.out.println("헤더 행 = " + header);

        assertEquals(3, header.size(), "헤더 컬럼 개수가 3(이름/이메일/전화번호)이 아닙니다.");

        /*
         * 7. 데이터 행 하나하나 실제 타입까지 출력
         *    - 전화번호가 숫자로 인식되어 Double로 오는지
         *    - 빈 셀이 null로 오는지, 아니면 행 자체가 짧게 잘려서 오는지(IndexOutOfBounds 위험)
         *    확인하는 것이 이 테스트의 핵심 목적입니다.
         */
        for (int i = 1; i < rows.size(); i++) {
            List<Object> row = rows.get(i);

            System.out.println("---- row " + (i + 1) + " ----");
            System.out.println("셀 개수 = " + row.size());

            for (int col = 0; col < row.size(); col++) {
                Object cell = row.get(col);
                String type = (cell == null) ? "null" : cell.getClass().getSimpleName();
                System.out.println("  [" + col + "] value=" + cell + " type=" + type);
            }

            if (row.size() < 3) {
                System.out.println("  ⚠ 이 행은 셀이 3개 미만입니다. "
                        + "getCell()에서 IndexOutOfBoundsException 방어 로직이 필요합니다.");
            }
        }

        /*
         * 8. 데이터 행이 최소 1개 이상 있는지 (헤더 제외)
         */
        assertFalse(
                rows.size() < 2,
                "헤더만 있고 데이터 행이 없습니다. 시트에 테스트 데이터를 추가하세요."
        );
    }
}
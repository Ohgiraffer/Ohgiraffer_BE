package com.ohgiraffer.evaluation.infrastructure.adapter;

import com.ohgiraffer.evaluation.application.port.EvaluationSheetReaderPort;
import com.ohgiraffer.global.google.sheets.GoogleSheetsClient;
import com.ohgiraffer.global.google.sheets.SpreadsheetIdExtractor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EvaluationSheetReaderAdapter implements EvaluationSheetReaderPort {

    /**
     * 한 번에 읽어올 범위.
     *
     * <p>넉넉히 잡아 한 번으로 끝낸다. 부족하다고 나눠 부르면 호출 횟수가 늘고,
     * 빈 행은 응답에 담기지 않아 넓게 잡아도 손해가 없다.
     */
    private static final String CELL_RANGE = "A1:Z1000";

    private final GoogleSheetsClient googleSheetsClient;
    private final SpreadsheetIdExtractor spreadsheetIdExtractor;

    public EvaluationSheetReaderAdapter(
            GoogleSheetsClient googleSheetsClient,
            SpreadsheetIdExtractor spreadsheetIdExtractor
    ) {
        this.googleSheetsClient = googleSheetsClient;
        this.spreadsheetIdExtractor = spreadsheetIdExtractor;
    }

    @Override
    public List<List<String>> readRows(String spreadsheetUrl, String tabName) {
        String spreadsheetId = spreadsheetIdExtractor.extract(spreadsheetUrl);

        List<List<Object>> rows = googleSheetsClient.readRange(
                spreadsheetId,
                toRange(tabName)
        );

        return rows.stream()
                .map(EvaluationSheetReaderAdapter::toStringRow)
                .toList();
    }

    /**
     * 탭 이름을 작은따옴표로 감싼다. 이름에 공백이 있으면 감싸지 않을 때 범위로 해석되지 않는다.
     */
    private static String toRange(String tabName) {
        return "'" + tabName.replace("'", "''") + "'!" + CELL_RANGE;
    }

    /**
     * 셀 값을 문자열로 바꾼다.
     *
     * <p>줄 끝의 빈 칸은 응답에 아예 담기지 않아 행마다 길이가 다르다. 읽는 쪽에서
     * 컬럼 위치로 접근하므로, 없는 자리를 빈 문자열로 채우지 않고 그대로 둔다.
     */
    private static List<String> toStringRow(List<Object> row) {
        List<String> values = new ArrayList<>(row.size());

        for (Object cell : row) {
            values.add(cell == null ? "" : cell.toString().trim());
        }

        return values;
    }
}

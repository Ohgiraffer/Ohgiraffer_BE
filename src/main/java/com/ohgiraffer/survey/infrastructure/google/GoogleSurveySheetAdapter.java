package com.ohgiraffer.survey.infrastructure.google;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.google.sheets.GoogleSheetsClient;
import com.ohgiraffer.global.google.sheets.SpreadsheetIdExtractor;
import com.ohgiraffer.survey.application.port.SurveySheetConnectionInfo;
import com.ohgiraffer.survey.application.port.SurveySheetPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class GoogleSurveySheetAdapter
        implements SurveySheetPort {

    private final SpreadsheetIdExtractor spreadsheetIdExtractor;
    private final GoogleSheetsClient googleSheetsClient;

    public GoogleSurveySheetAdapter(
            SpreadsheetIdExtractor spreadsheetIdExtractor,
            GoogleSheetsClient googleSheetsClient
    ) {
        this.spreadsheetIdExtractor = spreadsheetIdExtractor;
        this.googleSheetsClient = googleSheetsClient;
    }

    @Override
    public SurveySheetConnectionInfo inspect(
            String spreadsheetUrl,
            String requestedSheetName
    ) {
        String spreadsheetId =
                spreadsheetIdExtractor.extract(
                        spreadsheetUrl
                );

        Optional<Long> requestedGid =
                spreadsheetIdExtractor.extractGid(
                        spreadsheetUrl
                );

        String spreadsheetTitle =
                googleSheetsClient.getSpreadsheetTitle(
                        spreadsheetId
                );

        List<GoogleSheetsClient.SheetInfo> sheetInfos =
                googleSheetsClient.getSheetInfos(
                        spreadsheetId
                );

        if (sheetInfos.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "조회 가능한 Google Sheet가 없습니다."
            );
        }

        GoogleSheetsClient.SheetInfo selectedSheet =
                selectSheet(
                        sheetInfos,
                        requestedSheetName,
                        requestedGid
                );

        List<List<Object>> headerRows =
                googleSheetsClient.readRange(
                        spreadsheetId,
                        buildHeaderRange(
                                selectedSheet.name()
                        )
                );

        List<String> columns =
                extractColumns(
                        headerRows
                );

        if (columns.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "선택한 Google Sheet에 컬럼이 없습니다."
            );
        }

        List<SurveySheetConnectionInfo.SurveySheetInfo> sheets =
                sheetInfos.stream()
                        .map(sheetInfo ->
                                new SurveySheetConnectionInfo
                                        .SurveySheetInfo(
                                        sheetInfo.name(),
                                        sheetInfo.gid()
                                )
                        )
                        .toList();

        return new SurveySheetConnectionInfo(
                spreadsheetId,
                spreadsheetTitle,
                sheets,
                selectedSheet.name(),
                selectedSheet.gid(),
                columns
        );
    }

    private GoogleSheetsClient.SheetInfo selectSheet(
            List<GoogleSheetsClient.SheetInfo> sheetInfos,
            String requestedSheetName,
            Optional<Long> requestedGid
    ) {
        if (requestedSheetName != null
                && !requestedSheetName.isBlank()) {
            String normalizedSheetName =
                    requestedSheetName.trim();

            return sheetInfos.stream()
                    .filter(sheetInfo ->
                            sheetInfo.name()
                                    .equals(normalizedSheetName)
                    )
                    .findFirst()
                    .orElseThrow(
                            () -> new BusinessException(
                                    ErrorCode.INVALID_INPUT_VALUE,
                                    "선택한 Google Sheet를 찾을 수 없습니다."
                            )
                    );
        }

        if (requestedGid.isPresent()) {
            long gid = requestedGid.get();

            return sheetInfos.stream()
                    .filter(sheetInfo ->
                            sheetInfo.gid() == gid
                    )
                    .findFirst()
                    .orElseThrow(
                            () -> new BusinessException(
                                    ErrorCode.GOOGLE_SHEET_INVALID_URL,
                                    "URL의 gid에 해당하는 Google Sheet를 찾을 수 없습니다."
                            )
                    );
        }

        return sheetInfos.get(0);
    }

    private String buildHeaderRange(
            String sheetName
    ) {
        String escapedSheetName =
                sheetName.replace(
                        "'",
                        "''"
                );

        return "'"
                + escapedSheetName
                + "'!1:1";
    }

    private List<String> extractColumns(
            List<List<Object>> rows
    ) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        List<Object> headerRow =
                rows.get(0);

        if (headerRow == null
                || headerRow.isEmpty()) {
            return List.of();
        }

        return headerRow.stream()
                .map(this::toColumnName)
                .filter(column ->
                        !column.isBlank()
                )
                .toList();
    }

    private String toColumnName(
            Object value
    ) {
        if (value == null) {
            return "";
        }

        return value.toString().trim();
    }
}
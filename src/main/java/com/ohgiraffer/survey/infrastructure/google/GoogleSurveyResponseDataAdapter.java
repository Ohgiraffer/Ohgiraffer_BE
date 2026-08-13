package com.ohgiraffer.survey.infrastructure.google;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.google.sheets.GoogleSheetsClient;
import com.ohgiraffer.survey.application.port.SurveyResponseDataPort;
import com.ohgiraffer.survey.application.port.SurveyResponseDataset;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class GoogleSurveyResponseDataAdapter
        implements SurveyResponseDataPort {

    private final GoogleSheetsClient googleSheetsClient;

    public GoogleSurveyResponseDataAdapter(
            GoogleSheetsClient googleSheetsClient
    ) {
        this.googleSheetsClient = googleSheetsClient;
    }

    @Override
    public SurveyResponseDataset readResponses(
            String spreadsheetId,
            String spreadsheetTitle,
            String sheetName
    ) {
        try {
            return doReadResponses(
                    spreadsheetId,
                    spreadsheetTitle,
                    sheetName
            );

        } catch (BusinessException exception) {
            throw exception;

        } catch (RuntimeException exception) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_SHEET_API_ERROR,
                    exception
            );
        }
    }

    private SurveyResponseDataset doReadResponses(
            String spreadsheetId,
            String spreadsheetTitle,
            String sheetName
    ) {
        validateRequest(
                spreadsheetId,
                spreadsheetTitle,
                sheetName
        );

        String range =
                buildSheetRange(
                        sheetName
                );

        List<List<Object>> values =
                googleSheetsClient.readRange(
                        spreadsheetId,
                        range
                );

        if (values == null || values.isEmpty()) {
            return new SurveyResponseDataset(
                    spreadsheetTitle,
                    sheetName,
                    List.of(),
                    List.of()
            );
        }

        List<String> headers =
                convertRow(
                        values.get(0)
                );

        if (headers.isEmpty()) {
            return new SurveyResponseDataset(
                    spreadsheetTitle,
                    sheetName,
                    List.of(),
                    List.of()
            );
        }

        List<List<String>> responseRows =
                new ArrayList<>();

        for (int rowIndex = 1;
             rowIndex < values.size();
             rowIndex++) {

            List<String> row =
                    convertAndPadRow(
                            values.get(rowIndex),
                            headers.size()
                    );

            if (!isEmptyRow(row)) {
                responseRows.add(row);
            }
        }

        return new SurveyResponseDataset(
                spreadsheetTitle,
                sheetName,
                headers,
                responseRows
        );
    }

    private String buildSheetRange(String sheetName) {
        String escapedSheetName =
                sheetName.replace("'", "''");

        return "'"
                + escapedSheetName
                + "'";
    }

    private List<String> convertRow(
            List<Object> source
    ) {
        if (source == null) {
            return List.of();
        }

        return source.stream()
                .map(value ->
                        Objects.toString(value, "")
                )
                .toList();
    }

    private List<String> convertAndPadRow(
            List<Object> source,
            int headerCount
    ) {
        List<String> converted =
                new ArrayList<>(
                        convertRow(source)
                );

        while (converted.size() < headerCount) {
            converted.add("");
        }

        if (converted.size() > headerCount) {
            return List.copyOf(
                    converted.subList(
                            0,
                            headerCount
                    )
            );
        }

        return List.copyOf(converted);
    }

    private boolean isEmptyRow(
            List<String> row
    ) {
        return row.stream()
                .allMatch(value ->
                        value == null
                                || value.isBlank()
                );
    }

    private void validateRequest(
            String spreadsheetId,
            String spreadsheetTitle,
            String sheetName
    ) {
        if (spreadsheetId == null
                || spreadsheetId.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "Google Spreadsheet ID가 필요합니다."
            );
        }

        if (spreadsheetTitle == null
                || spreadsheetTitle.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "Google Spreadsheet 제목이 필요합니다."
            );
        }

        if (sheetName == null
                || sheetName.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "Google Sheet 이름이 필요합니다."
            );
        }
    }
}
package com.ohgiraffer.global.google.sheets;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;

public class GoogleSheetsClient {

    private final Sheets sheets;

    public GoogleSheetsClient(Sheets sheets) {
        this.sheets = sheets;
    }

    public List<List<Object>> readRange(
            String spreadsheetId,
            String range
    ) {
        validateSpreadsheetId(spreadsheetId);
        validateRange(range);

        try {
            ValueRange response = sheets
                    .spreadsheets()
                    .values()
                    .get(spreadsheetId, range)
                    .execute();

            if (response.getValues() == null) {
                return Collections.emptyList();
            }

            return response.getValues();

        } catch (GoogleJsonResponseException exception) {
            throw convertGoogleException(exception);

        } catch (IOException exception) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_SHEET_API_ERROR
            );
        }
    }

    public Map<String, List<List<Object>>> readRanges(
            String spreadsheetId,
            List<String> ranges
    ) {
        validateSpreadsheetId(spreadsheetId);

        if (ranges == null || ranges.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "조회 범위가 한 개 이상 필요합니다."
            );
        }

        for (String range : ranges) {
    validateRange(range);
}

if (new LinkedHashSet<>(ranges).size() != ranges.size()) {
    throw new BusinessException(
            ErrorCode.INVALID_INPUT_VALUE,
            "중복된 Google Sheets 조회 범위는 사용할 수 없습니다."
    );
}

        try {
            BatchGetValuesResponse response = sheets
                    .spreadsheets()
                    .values()
                    .batchGet(spreadsheetId)
                    .setRanges(ranges)
                    .execute();

            Map<String, List<List<Object>>> result =
                    new LinkedHashMap<>();

           List<ValueRange> valueRanges =
        response.getValueRanges();

for (int index = 0; index < ranges.size(); index++) {
    List<List<Object>> values =
            Collections.emptyList();

    if (valueRanges != null
            && index < valueRanges.size()
            && valueRanges.get(index) != null
            && valueRanges.get(index).getValues() != null) {
        values = valueRanges
                .get(index)
                .getValues();
    }

    result.put(
            ranges.get(index),
            values
    );
}

            return result;

        } catch (GoogleJsonResponseException exception) {
            throw convertGoogleException(exception);

        } catch (IOException exception) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_SHEET_API_ERROR
            );
        }
    }

   public String getSpreadsheetTitle(
        String spreadsheetId
) {
    validateSpreadsheetId(spreadsheetId);

    try {
        Spreadsheet spreadsheet = sheets
                .spreadsheets()
                .get(spreadsheetId)
                .setFields("properties.title")
                .execute();

        if (spreadsheet == null
                || spreadsheet.getProperties() == null
                || spreadsheet.getProperties().getTitle() == null
                || spreadsheet.getProperties().getTitle().isBlank()) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_SHEET_API_ERROR
            );
        }

        return spreadsheet
                .getProperties()
                .getTitle();

    } catch (GoogleJsonResponseException exception) {
        throw convertGoogleException(exception);

    } catch (IOException exception) {
        throw new BusinessException(
                ErrorCode.GOOGLE_SHEET_API_ERROR
        );
    }
}

    private void validateSpreadsheetId(
            String spreadsheetId
    ) {
        if (spreadsheetId == null
                || spreadsheetId.isBlank()) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_SHEET_INVALID_URL,
                    "스프레드시트 ID가 필요합니다."
            );
        }
    }

    private void validateRange(
            String range
    ) {
        if (range == null || range.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "Google 시트 조회 범위가 필요합니다."
            );
        }
    }

    private BusinessException convertGoogleException(
            GoogleJsonResponseException exception
    ) {
        return switch (exception.getStatusCode()) {
            case 403 -> new BusinessException(
                    ErrorCode.GOOGLE_SHEET_ACCESS_DENIED
            );
            case 404 -> new BusinessException(
                    ErrorCode.GOOGLE_SHEET_NOT_FOUND
            );
            case 429 -> new BusinessException(
                    ErrorCode.GOOGLE_SHEET_RATE_LIMIT_EXCEEDED
            );
            default -> new BusinessException(
                    ErrorCode.GOOGLE_SHEET_API_ERROR
            );
        };
    }
}

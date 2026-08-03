package com.ohgiraffer.global.google.sheets;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        try {
            BatchGetValuesResponse response = sheets
                    .spreadsheets()
                    .values()
                    .batchGet(spreadsheetId)
                    .setRanges(ranges)
                    .execute();

            Map<String, List<List<Object>>> result =
                    new LinkedHashMap<>();

            if (response.getValueRanges() == null) {
                return result;
            }

            for (ValueRange valueRange
                    : response.getValueRanges()) {

                List<List<Object>> values =
                        valueRange.getValues() == null
                                ? Collections.emptyList()
                                : valueRange.getValues();

                result.put(
                        valueRange.getRange(),
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
            return sheets
                    .spreadsheets()
                    .get(spreadsheetId)
                    .setFields("properties.title")
                    .execute()
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
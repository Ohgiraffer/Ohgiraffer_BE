package com.ohgiraffer.global.google.sheets;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SpreadsheetIdExtractor {

    private static final Pattern SPREADSHEET_URL_PATTERN =
            Pattern.compile(
                    "^https://docs\\.google\\.com/spreadsheets/d/"
                            + "([a-zA-Z0-9_-]+)"
                            + "(?:/.*)?$"
            );

    public String extract(String spreadsheetUrl) {
        if (spreadsheetUrl == null
                || spreadsheetUrl.isBlank()) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_SHEET_INVALID_URL
            );
        }

        Matcher matcher =
                SPREADSHEET_URL_PATTERN.matcher(
                        spreadsheetUrl.trim()
                );

        if (!matcher.matches()) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_SHEET_INVALID_URL
            );
        }

        return matcher.group(1);
    }
}
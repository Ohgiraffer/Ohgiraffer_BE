package com.ohgiraffer.global.google.sheets;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SpreadsheetIdExtractor {

   private static final Pattern SPREADSHEET_URL_PATTERN =
        Pattern.compile(
                "^https://docs\\.google\\.com/spreadsheets/"
                        + "(?:u/\\d+/)?"
                        + "d/"
                        + "([a-zA-Z0-9_-]+)"
                        + "(?:/[^?#]*)?"
                        + "(?:[?#].*)?$"
        );

    private static final Pattern GID_PATTERN =
            Pattern.compile("[?#&]gid=(\\d+)");

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

    public Optional<Long> extractGid(String spreadsheetUrl) {
        if (spreadsheetUrl == null) {
            return Optional.empty();
        }

        Matcher matcher = GID_PATTERN.matcher(spreadsheetUrl);
        if (matcher.find()) {
            return Optional.of(Long.parseLong(matcher.group(1)));
        }
        return Optional.empty();
    }
}

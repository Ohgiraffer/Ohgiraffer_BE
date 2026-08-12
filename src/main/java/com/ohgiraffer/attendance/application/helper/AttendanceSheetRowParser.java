package com.ohgiraffer.attendance.application.helper;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AttendanceSheetRowParser {

    private static final Pattern SPREADSHEET_ID_PATTERN = Pattern.compile("/d/([a-zA-Z0-9-_]+)");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    private Map<String, Integer> buildColumnIndex(List<Object> headerRow, Map<String, String> columnMapping) {
        Map<String, Integer> result = new HashMap<>();

        for (Map.Entry<String, String> entry : columnMapping.entrySet()) {
            String fieldName = entry.getKey();
            String headerLabel = entry.getValue();

            for (int i = 0; i < headerRow.size(); i++) {
                if (headerLabel.equals(String.valueOf(headerRow.get(i)).trim())) {
                    result.put(fieldName, i);
                    break;
                }
            }
        }

        if (!result.containsKey("name")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "컬럼매핑에 name이 없습니다.");
        }
        return result;
    }

    public String cell(List<Object> row, Integer index) {
        if (index == null || index >= row.size()) {
            return "";
        }
        Object value = row.get(index);
        return value == null ? "" : String.valueOf(value).trim();
    }

    public LocalTime parseTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(raw.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "시간 형식이 올바르지 않습니다: " + raw);
        }
    }

    public String extractSpreadsheetId(String sheetUrl) {
        Matcher matcher = SPREADSHEET_ID_PATTERN.matcher(sheetUrl);
        if (!matcher.find()) {
            throw new BusinessException(ErrorCode.GOOGLE_SHEET_INVALID_URL);
        }
        return matcher.group(1);
    }
}
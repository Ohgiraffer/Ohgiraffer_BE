package com.ohgiraffer.survey.application.port;

import java.util.List;

public record SurveyResponseDataset(
        String spreadsheetTitle,
        String sheetName,
        List<String> headers,
        List<List<String>> rows
) {

    public SurveyResponseDataset {
        headers = headers == null
                ? List.of()
                : List.copyOf(headers);

        rows = rows == null
                ? List.of()
                : rows.stream()
                .map(List::copyOf)
                .toList();
    }

    public int responseCount() {
        return rows.size();
    }

    public boolean isEmpty() {
        return headers.isEmpty() || rows.isEmpty();
    }
}
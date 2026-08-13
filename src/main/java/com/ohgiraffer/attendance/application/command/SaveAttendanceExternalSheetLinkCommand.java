package com.ohgiraffer.attendance.application.command;

import java.util.Map;

public record SaveAttendanceExternalSheetLinkCommand(
        String sheetUrl,
        String tabName,
        String dateCellRange,
        Map<String, String> columnMapping
) {
}
package com.ohgiraffer.attendance.application.command;

import com.ohgiraffer.attendance.domain.model.SyncTriggerType;

public record SyncAttendanceSheetCommand(
        Long userId,
        SyncTriggerType trigger
) {
}
package com.ohgiraffer.attendance.application.usecase;

import com.ohgiraffer.attendance.application.command.SaveAttendanceExternalSheetLinkCommand;
import com.ohgiraffer.attendance.application.command.SyncAttendanceSheetCommand;
import com.ohgiraffer.attendance.domain.dto.AttendanceExternalSheetLinkView;
import com.ohgiraffer.attendance.domain.dto.SyncAttendanceSheetResult;

public interface AttendanceSheetCommandUsecase {

    AttendanceExternalSheetLinkView save(SaveAttendanceExternalSheetLinkCommand command);

    SyncAttendanceSheetResult sync(SyncAttendanceSheetCommand command);

    void cleanupLogs();
}
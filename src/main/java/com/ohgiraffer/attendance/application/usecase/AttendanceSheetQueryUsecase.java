package com.ohgiraffer.attendance.application.usecase;

import com.ohgiraffer.attendance.domain.dto.AttendanceSheetSyncLogView;

import java.util.List;

public interface AttendanceSheetQueryUsecase {

    List<AttendanceSheetSyncLogView> getLogs();
}
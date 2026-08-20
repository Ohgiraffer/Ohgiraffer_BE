package com.ohgiraffer.attendance.domain.repository;

import com.ohgiraffer.attendance.domain.model.AttendanceSheetSyncLog;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceSheetSyncLogRepository {

    AttendanceSheetSyncLog save(AttendanceSheetSyncLog attendanceSheetSyncLog);

    List<AttendanceSheetSyncLog> findAll();

    void deleteBefore(LocalDateTime cutoff);
}
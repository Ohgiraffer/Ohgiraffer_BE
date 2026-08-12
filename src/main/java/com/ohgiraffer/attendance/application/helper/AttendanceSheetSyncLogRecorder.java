package com.ohgiraffer.attendance.application.helper;

import com.ohgiraffer.attendance.application.command.RecordAttendanceSheetSyncLogCommand;
import com.ohgiraffer.attendance.domain.model.AttendanceSheetSyncLog;
import com.ohgiraffer.attendance.domain.repository.AttendanceExternalSheetLinkRepository;
import com.ohgiraffer.attendance.domain.repository.AttendanceSheetSyncLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AttendanceSheetSyncLogRecorder {

    private final AttendanceSheetSyncLogRepository attendanceSheetSyncLogRepository;
    private final AttendanceExternalSheetLinkRepository attendanceExternalSheetLinkRepository;

    public void record(RecordAttendanceSheetSyncLogCommand command) {
        AttendanceSheetSyncLog logEntry = AttendanceSheetSyncLog.builder()
                .attendanceSheetLinkId(command.attendanceSheetLinkId())
                .changedRange(command.changedRange())
                .diffSummary(command.diffSummary())
                .failedRowDetails(command.failedRowDetails())
                .executorId(command.executorId())
                .executorName(command.executorName())
                .result(command.result())
                .build();

        attendanceSheetSyncLogRepository.save(logEntry);

        if (command.attendanceSheetLinkId() != null) {
            attendanceExternalSheetLinkRepository.findById(command.attendanceSheetLinkId())
                    .ifPresent(link -> attendanceExternalSheetLinkRepository.save(link.withSyncedNow(LocalDateTime.now())));
        }
    }
}
package com.ohgiraffer.attendance.application.helper;

import com.ohgiraffer.attendance.domain.model.AttendanceSyncOutcome;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AttendanceSheetRowSyncer {

    private final AttendanceConflictRetrySaver attendanceConflictRetrySaver;

    public AttendanceSyncOutcome syncOneRow(
            Long userId, LocalDate targetDate, List<Object> row,
            Map<String, Integer> columnIndex, boolean provisional
    ) {
        try {
            return attendanceConflictRetrySaver.syncOneRowInNewTransaction(userId, targetDate, row, columnIndex, provisional);
        } catch (DataIntegrityViolationException e) {
            return attendanceConflictRetrySaver.syncOneRowInNewTransaction(userId, targetDate, row, columnIndex, provisional);
        }
    }
}
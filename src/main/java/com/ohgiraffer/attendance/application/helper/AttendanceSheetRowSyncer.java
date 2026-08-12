package com.ohgiraffer.attendance.application.helper;

import com.ohgiraffer.attendance.domain.model.Attendance;
import com.ohgiraffer.attendance.domain.model.AttendanceStatus;
import com.ohgiraffer.attendance.domain.model.AttendanceSyncOutcome;
import com.ohgiraffer.attendance.domain.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AttendanceSheetRowSyncer {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceStatusResolver attendanceStatusResolver;
    private final AttendanceSheetRowParser attendanceSheetRowParser;

    public AttendanceSyncOutcome syncOneRow(
            Long userId, LocalDate targetDate, List<Object> row,
            Map<String, Integer> columnIndex, boolean provisional
    ) {
        Optional<Attendance> existing = attendanceRepository.findByUserIdAndDateForUpdate(userId, targetDate);

        if (existing.isPresent()
                && (existing.get().getStatus() == AttendanceStatus.LEAVE
                || existing.get().getStatus() == AttendanceStatus.SICK)) {
            return AttendanceSyncOutcome.SKIPPED;
        }

        LocalTime checkInTime = attendanceSheetRowParser.parseTime(attendanceSheetRowParser.cell(row, columnIndex.get("checkIn")));
        LocalTime checkOutTime = attendanceSheetRowParser.parseTime(attendanceSheetRowParser.cell(row, columnIndex.get("checkOut")));
        LocalTime outingTime = attendanceSheetRowParser.parseTime(attendanceSheetRowParser.cell(row, columnIndex.get("outing")));
        LocalTime returnTime = attendanceSheetRowParser.parseTime(attendanceSheetRowParser.cell(row, columnIndex.get("return")));

        AttendanceStatus status;
        if (provisional) {
            status = attendanceStatusResolver.resolveProvisional(checkInTime);
            if (status == null) {
                return AttendanceSyncOutcome.SKIPPED;
            }
        } else {
            status = attendanceStatusResolver.resolve(checkInTime, checkOutTime, outingTime, returnTime);
        }

        boolean changed = existing.isEmpty()
                || existing.get().getStatus() != status
                || !Objects.equals(existing.get().getCheckInTime(), checkInTime)
                || !Objects.equals(existing.get().getCheckOutTime(), checkOutTime)
                || !Objects.equals(existing.get().getOutingTime(), outingTime)
                || !Objects.equals(existing.get().getReturnTime(), returnTime);

        if (!changed) {
            return AttendanceSyncOutcome.UNCHANGED;
        }

        Attendance attendance = existing.isPresent()
                ? Attendance.reconstitute(existing.get().getId(), userId, targetDate, status,
                checkInTime, checkOutTime, outingTime, returnTime, null)
                : Attendance.create(userId, targetDate, status,
                checkInTime, checkOutTime, outingTime, returnTime, null);

        try {
            attendanceRepository.save(attendance);
        } catch (DataIntegrityViolationException e) {
            Attendance reloaded = attendanceRepository.findByUserIdAndDate(userId, targetDate)
                    .orElseThrow(() -> e);
            Attendance retry = Attendance.reconstitute(reloaded.getId(), userId, targetDate, status,
                    checkInTime, checkOutTime, outingTime, returnTime, null);
            attendanceRepository.save(retry);
        }

        return AttendanceSyncOutcome.CHANGED;
    }
}
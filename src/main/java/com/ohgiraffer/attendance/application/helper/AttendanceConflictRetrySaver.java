package com.ohgiraffer.attendance.application.helper;

import com.ohgiraffer.attendance.domain.model.Attendance;
import com.ohgiraffer.attendance.domain.model.AttendanceStatus;
import com.ohgiraffer.attendance.domain.model.AttendanceSyncOutcome;
import com.ohgiraffer.attendance.domain.repository.AttendanceRepository;
import com.ohgiraffer.attendance.domain.repository.SickBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class AttendanceConflictRetrySaver {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceStatusResolver attendanceStatusResolver;
    private final AttendanceSheetRowParser attendanceSheetRowParser;
    private final SickBalanceRepository sickBalanceRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AttendanceSyncOutcome syncOneRowInNewTransaction(
            Long userId, LocalDate targetDate, List<Object> row,
            Map<String, Integer> columnIndex, boolean provisional
    ) {
        Optional<Attendance> existing = attendanceRepository.findByUserIdAndDateForUpdate(userId, targetDate);

        if (existing.isPresent()
                && (existing.get().getStatus() == AttendanceStatus.LEAVE
                || existing.get().getStatus() == AttendanceStatus.SICK)) {
            return AttendanceSyncOutcome.SKIPPED;
        }

        String rawLabel = attendanceSheetRowParser.cell(row, columnIndex.get("attendanceStatus"));
        AttendanceStatus status = attendanceStatusResolver.resolve(rawLabel);
        if (status == null) {
            return AttendanceSyncOutcome.SKIPPED;
        }

        LocalTime checkInTime = attendanceSheetRowParser.parseTime(attendanceSheetRowParser.cell(row, columnIndex.get("checkIn")));
        LocalTime checkOutTime = attendanceSheetRowParser.parseTime(attendanceSheetRowParser.cell(row, columnIndex.get("checkOut")));
        LocalTime outingTime = attendanceSheetRowParser.parseTime(attendanceSheetRowParser.cell(row, columnIndex.get("outing")));
        LocalTime returnTime = attendanceSheetRowParser.parseTime(attendanceSheetRowParser.cell(row, columnIndex.get("return")));

        boolean changed = existing.isEmpty()
                || existing.get().getStatus() != status
                || !Objects.equals(existing.get().getCheckInTime(), checkInTime)
                || !Objects.equals(existing.get().getCheckOutTime(), checkOutTime)
                || !Objects.equals(existing.get().getOutingTime(), outingTime)
                || !Objects.equals(existing.get().getReturnTime(), returnTime);

        if (!changed) {
            return AttendanceSyncOutcome.UNCHANGED;
        }

        String existingExternalRefId = existing.map(Attendance::getExternalRefId).orElse(null);

        Attendance attendance = existing.isPresent()
                ? Attendance.reconstitute(existing.get().getId(), userId, targetDate, status,
                checkInTime, checkOutTime, outingTime, returnTime, existingExternalRefId)
                : Attendance.create(userId, targetDate, status,
                checkInTime, checkOutTime, outingTime, returnTime, null);

        attendanceRepository.save(attendance);

        if (status == AttendanceStatus.SICK) {
            boolean consumed = sickBalanceRepository.tryConsume(userId, BigDecimal.ONE);
            if (!consumed) {
                log.warn("[syncOneRowInNewTransaction] 병결 잔여 부족 — 소진 실패, 출결은 SICK으로 기록됨 | userId={}, date={}",
                        userId, targetDate);
            }
        }

        return AttendanceSyncOutcome.CHANGED;
    }
}
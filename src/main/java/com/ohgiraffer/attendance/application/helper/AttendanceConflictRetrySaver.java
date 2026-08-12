package com.ohgiraffer.attendance.application.helper;

import com.ohgiraffer.attendance.domain.model.Attendance;
import com.ohgiraffer.attendance.domain.model.AttendanceStatus;
import com.ohgiraffer.attendance.domain.model.AttendanceSyncOutcome;
import com.ohgiraffer.attendance.domain.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

// 행 하나의 조회(비관적 락)부터 저장까지를 하나의 격리된 트랜잭션으로 처리한다.
// sync() 전체를 감싸는 상위 트랜잭션과 완전히 분리되어 있어서 이 행에서 발생한
// 제약 위반이 배치 전체의 커밋을 막지 않는다. 실패 시 이 트랜잭션 자체가
// 정상적으로 롤백되며 락도 그 즉시 풀리므로 호출부에서 이 메서드를 한 번 더
// 호출하는 것만으로 안전하게 재시도할 수 있다.
@Component
@RequiredArgsConstructor
public class AttendanceConflictRetrySaver {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceStatusResolver attendanceStatusResolver;
    private final AttendanceSheetRowParser attendanceSheetRowParser;

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

        String existingExternalRefId = existing.map(Attendance::getExternalRefId).orElse(null);

        Attendance attendance = existing.isPresent()
                ? Attendance.reconstitute(existing.get().getId(), userId, targetDate, status,
                checkInTime, checkOutTime, outingTime, returnTime, existingExternalRefId)
                : Attendance.create(userId, targetDate, status,
                checkInTime, checkOutTime, outingTime, returnTime, null);

        attendanceRepository.save(attendance);

        return AttendanceSyncOutcome.CHANGED;
    }
}
package com.ohgiraffer.attendance.application.helper;

import com.ohgiraffer.attendance.domain.model.AttendanceStatus;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;

@Component
public class AttendanceStatusResolver {

    private static final LocalTime LATE_CUTOFF = LocalTime.of(9, 40);
    private static final LocalTime EARLY_LEAVE_CUTOFF = LocalTime.of(18, 20);
    private static final int MIN_ATTENDED_MINUTES = 240; // 소정훈련시간(8h)의 50%

    public AttendanceStatus resolve(
            LocalTime checkInTime,
            LocalTime checkOutTime,
            LocalTime outingTime,
            LocalTime returnTime
    ) {
        if (checkOutTime == null || checkInTime == null) {
            return AttendanceStatus.ABSENT;
        }

        if (checkOutTime.isBefore(checkInTime)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "퇴실 시간이 입실 시간보다 빠릅니다: 입실=" + checkInTime + ", 퇴실=" + checkOutTime);
        }

        long totalMinutes = Duration.between(checkInTime, checkOutTime).toMinutes();

        if (outingTime != null && returnTime != null && returnTime.isAfter(outingTime)) {
            totalMinutes -= Duration.between(outingTime, returnTime).toMinutes();
        }

        if (totalMinutes < MIN_ATTENDED_MINUTES) {
            return AttendanceStatus.ABSENT;
        }

        if (checkInTime.isAfter(LATE_CUTOFF)) {
            return AttendanceStatus.LATE;
        }

        if (checkOutTime.isBefore(EARLY_LEAVE_CUTOFF)) {
            return AttendanceStatus.EARLY_LEAVE;
        }

        if (outingTime != null || returnTime != null) {
            return AttendanceStatus.OUTING;
        }

        return AttendanceStatus.PRESENT;
    }

    // 입실 기록이 아직 없으면 null
    public AttendanceStatus resolveProvisional(LocalTime checkInTime) {
        if (checkInTime == null) {
            return null;
        }
        return checkInTime.isAfter(LATE_CUTOFF) ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;
    }
}
package com.ohgiraffer.attendance.application.helper;

import com.ohgiraffer.attendance.domain.model.Attendance;
import com.ohgiraffer.attendance.domain.model.AttendanceStatus;
import com.ohgiraffer.attendance.domain.repository.AttendanceRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class AttendanceConflictRetrySaver {

    private final AttendanceRepository attendanceRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void retrySave(
            Long userId, LocalDate targetDate, AttendanceStatus status,
            LocalTime checkInTime, LocalTime checkOutTime,
            LocalTime outingTime, LocalTime returnTime, String externalRefId
    ) {
        Attendance reloaded = attendanceRepository.findByUserIdAndDate(userId, targetDate)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE, "동시 저장 충돌 후 기존 출결을 찾을 수 없습니다."));

        Attendance retry = Attendance.reconstitute(reloaded.getId(), userId, targetDate, status,
                checkInTime, checkOutTime, outingTime, returnTime, externalRefId);
        attendanceRepository.save(retry);
    }
}
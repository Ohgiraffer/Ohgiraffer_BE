package com.ohgiraffer.attendance.domain.model;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class Attendance {
    private final Long id;
    private final Long userId;
    private final LocalDate attendanceDate;
    private final AttendanceStatus status;
    private final LocalTime checkInTime;
    private final LocalTime checkOutTime;
    private final LocalTime outingTime;
    private final LocalTime returnTime;
    private final String externalRefId;

    private Attendance(Long id, Long userId, LocalDate attendanceDate, AttendanceStatus status,
                       LocalTime checkInTime, LocalTime checkOutTime,
                       LocalTime outingTime, LocalTime returnTime, String externalRefId) {
        this.id = id;
        this.userId = userId;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.outingTime = outingTime;
        this.returnTime = returnTime;
        this.externalRefId = externalRefId;
    }

    // 신규 생성용
    public static Attendance create(Long userId, LocalDate attendanceDate, AttendanceStatus status,
                                    LocalTime checkInTime, LocalTime checkOutTime,
                                    LocalTime outingTime, LocalTime returnTime, String externalRefId) {
        return new Attendance(null, userId, attendanceDate, status, checkInTime, checkOutTime, outingTime, returnTime, externalRefId);
    }

    // DB에서 복원할 때 사용
    public static Attendance reconstitute(Long id, Long userId, LocalDate attendanceDate, AttendanceStatus status,
                                          LocalTime checkInTime, LocalTime checkOutTime,
                                          LocalTime outingTime, LocalTime returnTime, String externalRefId) {
        return new Attendance(id, userId, attendanceDate, status, checkInTime, checkOutTime, outingTime, returnTime, externalRefId);
    }
}
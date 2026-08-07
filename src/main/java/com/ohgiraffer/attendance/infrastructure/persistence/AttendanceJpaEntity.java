package com.ohgiraffer.attendance.infrastructure.persistence;

import com.ohgiraffer.attendance.domain.model.Attendance;
import com.ohgiraffer.attendance.domain.model.AttendanceStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;

    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    @Column(name = "check_out_time")
    private LocalTime checkOutTime;

    @Column(name = "external_ref_id")
    private String externalRefId;

    private AttendanceJpaEntity(Long id, Long userId, LocalDate attendanceDate, AttendanceStatus status,
                                LocalTime checkInTime, LocalTime checkOutTime, String externalRefId) {
        this.id = id;
        this.userId = userId;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.externalRefId = externalRefId;
    }

    public static AttendanceJpaEntity fromDomain(Attendance attendance) {
        return new AttendanceJpaEntity(
                attendance.getId(),
                attendance.getUserId(),
                attendance.getAttendanceDate(),
                attendance.getStatus(),
                attendance.getCheckInTime(),
                attendance.getCheckOutTime(),
                attendance.getExternalRefId()
        );
    }

    public Attendance toDomain() {
        return Attendance.reconstitute(
                id, userId, attendanceDate, status, checkInTime, checkOutTime, externalRefId
        );
    }
}
package com.ohgiraffer.consultation.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "counselor_available_time")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CounselorAvailableTimeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "available_date_id", nullable = false)
    private Long availableDateId;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    private CounselorAvailableTimeJpaEntity(Long availableDateId, LocalTime startTime) {
        this.availableDateId = availableDateId;
        this.startTime = startTime;
    }

    public static CounselorAvailableTimeJpaEntity of(Long availableDateId, LocalTime startTime) {
        return new CounselorAvailableTimeJpaEntity(availableDateId, startTime);
    }
}
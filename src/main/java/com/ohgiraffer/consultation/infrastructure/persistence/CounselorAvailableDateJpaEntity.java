package com.ohgiraffer.consultation.infrastructure.persistence;

import com.ohgiraffer.consultation.domain.model.CounselorAvailableDate;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "counselor_available_date")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CounselorAvailableDateJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "counselor_id", nullable = false)
    private Long counselorId;

    @Column(name = "available_date", nullable = false)
    private LocalDate availableDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private CounselorAvailableDateJpaEntity(Long id, Long counselorId, LocalDate availableDate, LocalDateTime createdAt) {
        this.id = id;
        this.counselorId = counselorId;
        this.availableDate = availableDate;
        this.createdAt = createdAt;
    }

    public static CounselorAvailableDateJpaEntity fromDomain(CounselorAvailableDate domain) {
        return new CounselorAvailableDateJpaEntity(
                domain.getId(),
                domain.getCounselorId(),
                domain.getAvailableDate(),
                domain.getCreatedAt()
        );
    }

    public CounselorAvailableDate toDomain(List<LocalTime> times) {
        return CounselorAvailableDate.builder()
                .id(id)
                .counselorId(counselorId)
                .availableDate(availableDate)
                .times(times)
                .createdAt(createdAt)
                .build();
    }

    @PrePersist
    private void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
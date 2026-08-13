package com.ohgiraffer.approval.infrastructure.persistence;

import com.ohgiraffer.approval.domain.model.profile.ApprovalApplicantProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "approval_applicant_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalApplicantProfileJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_applicant_profile_id")
    private Long id;

    @Column(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private Long userId;

    @Column(
            name = "birth_date",
            nullable = false
    )
    private LocalDate birthDate;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    private ApprovalApplicantProfileJpaEntity(
            Long id,
            Long userId,
            LocalDate birthDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.birthDate = birthDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ApprovalApplicantProfileJpaEntity from(
            ApprovalApplicantProfile profile
    ) {
        return new ApprovalApplicantProfileJpaEntity(
                profile.getId(),
                profile.getUserId(),
                profile.getBirthDate(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    public ApprovalApplicantProfile toDomain() {
        return ApprovalApplicantProfile.restore(
                id,
                userId,
                birthDate,
                createdAt,
                updatedAt
        );
    }
}
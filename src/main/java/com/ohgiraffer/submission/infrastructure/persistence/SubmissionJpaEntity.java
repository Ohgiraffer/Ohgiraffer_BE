package com.ohgiraffer.submission.infrastructure.persistence;

import com.ohgiraffer.global.entity.BaseTimeEntity;
import com.ohgiraffer.submission.domain.model.Submission;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "submission")
public class SubmissionJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long id;

    @Column(
            name = "submission_box_id",
            nullable = false
    )
    private Long submissionBoxId;

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "team_id")
    private Long teamId;

    @Column(
            name = "submitted_by",
            nullable = false
    )
    private Long submittedBy;

    @Column(
            name = "submitted_at",
            nullable = false
    )
    private LocalDateTime submittedAt;

    @Column(
            name = "is_late",
            nullable = false
    )
    private boolean late;

    @OneToMany(
            mappedBy = "submission",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<SubmissionItemValueJpaEntity> itemValues =
            new ArrayList<>();

    protected SubmissionJpaEntity() {
    }

    private SubmissionJpaEntity(
            Long id,
            Long submissionBoxId,
            Long ownerUserId,
            Long teamId,
            Long submittedBy,
            LocalDateTime submittedAt,
            boolean late
    ) {
        this.id = id;
        this.submissionBoxId = submissionBoxId;
        this.ownerUserId = ownerUserId;
        this.teamId = teamId;
        this.submittedBy = submittedBy;
        this.submittedAt = submittedAt;
        this.late = late;
    }

    public static SubmissionJpaEntity from(
            Submission submission
    ) {
        SubmissionJpaEntity entity =
                new SubmissionJpaEntity(
                        submission.getId(),
                        submission.getSubmissionBoxId(),
                        submission.getOwnerUserId(),
                        submission.getTeamId(),
                        submission.getSubmittedBy(),
                        submission.getSubmittedAt(),
                        submission.isLate()
                );

        submission.getItemValues()
                .stream()
                .map(SubmissionItemValueJpaEntity::from)
                .forEach(entity::addItemValue);

        return entity;
    }

    private void addItemValue(
            SubmissionItemValueJpaEntity value
    ) {
        itemValues.add(value);
        value.assignSubmission(this);
    }

    public Submission toDomain() {
        return Submission.restore(
                id,
                submissionBoxId,
                ownerUserId,
                teamId,
                submittedBy,
                submittedAt,
                late,
                itemValues.stream()
                        .map(SubmissionItemValueJpaEntity::toDomain)
                        .toList(),
                getCreatedAt(),
                getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }
}
package com.ohgiraffer.submissionbox.infrastructure.persistence;

import com.ohgiraffer.global.entity.BaseTimeEntity;
import com.ohgiraffer.submissionbox.domain.model.LatePolicy;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;
import com.ohgiraffer.submissionbox.domain.model.SubmissionTargetScope;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "submission_box")
public class SubmissionBoxJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_box_id")
    private Long id;

    @Column(
            name = "project_name",
            nullable = false,
            length = 255
    )
    private String projectName;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "target_scope",
            nullable = false,
            length = 20
    )
    private SubmissionTargetScope targetScope;

    @Column(
            name = "start_at",
            nullable = false
    )
    private LocalDateTime startAt;

    @Column(
            name = "due_at",
            nullable = false
    )
    private LocalDateTime dueAt;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "late_policy",
            nullable = false,
            length = 20
    )
    private LatePolicy latePolicy;

    @Column(
            name = "created_by",
            nullable = false
    )
    private Long createdBy;

    @OneToMany(
            mappedBy = "submissionBox",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("sortOrder ASC")
    private List<SubmissionBoxItemJpaEntity> items = new ArrayList<>();

    protected SubmissionBoxJpaEntity() {
    }

    private SubmissionBoxJpaEntity(
            Long id,
            String projectName,
            SubmissionTargetScope targetScope,
            LocalDateTime startAt,
            LocalDateTime dueAt,
            LatePolicy latePolicy,
            Long createdBy
    ) {
        this.id = id;
        this.projectName = projectName;
        this.targetScope = targetScope;
        this.startAt = startAt;
        this.dueAt = dueAt;
        this.latePolicy = latePolicy;
        this.createdBy = createdBy;
    }

    public static SubmissionBoxJpaEntity from(
            SubmissionBox submissionBox
    ) {
        SubmissionBoxJpaEntity entity =
                new SubmissionBoxJpaEntity(
                        submissionBox.getId(),
                        submissionBox.getProjectName(),
                        submissionBox.getTargetScope(),
                        submissionBox.getStartAt(),
                        submissionBox.getDueAt(),
                        submissionBox.getLatePolicy(),
                        submissionBox.getCreatedBy()
                );

        submissionBox.getItems()
                .stream()
                .map(SubmissionBoxItemJpaEntity::from)
                .forEach(entity::addItem);

        return entity;
    }

    private void addItem(
            SubmissionBoxItemJpaEntity item
    ) {
        items.add(item);
        item.assignSubmissionBox(this);
    }

    public SubmissionBox toDomain() {
        return SubmissionBox.restore(
                id,
                projectName,
                targetScope,
                startAt,
                dueAt,
                latePolicy,
                createdBy,
                items.stream()
                        .map(SubmissionBoxItemJpaEntity::toDomain)
                        .toList(),
                getCreatedAt(),
                getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }
}
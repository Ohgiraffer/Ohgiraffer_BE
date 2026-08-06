package com.ohgiraffer.submissionbox.infrastructure.persistence;

import com.ohgiraffer.global.entity.BaseTimeEntity;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.submissionbox.domain.model.LatePolicy;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBoxItem;
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
import java.util.*;

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

    public void updateFrom(
            SubmissionBox submissionBox
    ) {
        this.projectName = submissionBox.getProjectName();
        this.targetScope = submissionBox.getTargetScope();
        this.startAt = submissionBox.getStartAt();
        this.dueAt = submissionBox.getDueAt();
        this.latePolicy = submissionBox.getLatePolicy();

        synchronizeItems(submissionBox.getItems());
    }

    private void synchronizeItems(
            List<SubmissionBoxItem> updatedItems
    ) {
        Map<Long, SubmissionBoxItemJpaEntity> existingItemMap =
                new HashMap<>();

        for (SubmissionBoxItemJpaEntity existingItem : items) {
            existingItemMap.put(
                    existingItem.getId(),
                    existingItem
            );
        }

        Set<Long> retainedItemIds = new HashSet<>();

        for (SubmissionBoxItem updatedItem : updatedItems) {
            Long updatedItemId = updatedItem.getId();

            if (updatedItemId == null) {
                SubmissionBoxItemJpaEntity newItem =
                        SubmissionBoxItemJpaEntity.from(updatedItem);

                addItem(newItem);
                continue;
            }

            SubmissionBoxItemJpaEntity existingItem =
                    existingItemMap.get(updatedItemId);

            if (existingItem == null) {
                throw new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "해당 제출함에 존재하지 않는 제출 항목입니다."
                );
            }

            existingItem.updateFrom(updatedItem);
            retainedItemIds.add(updatedItemId);
        }

        items.removeIf(item ->
                item.getId() != null
                        && !retainedItemIds.contains(item.getId())
        );
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
package com.ohgiraffer.submission.infrastructure.persistence;

import com.ohgiraffer.global.entity.BaseTimeEntity;
import com.ohgiraffer.submission.domain.model.Submission;
import com.ohgiraffer.submission.domain.model.SubmissionItemValue;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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

    public void updateFrom(
            Submission submission
    ) {
        this.submittedBy =
                submission.getSubmittedBy();

        this.submittedAt =
                submission.getSubmittedAt();

        this.late =
                submission.isLate();

        Map<Long, SubmissionItemValueJpaEntity>
                existingByBoxItemId =
                new HashMap<>();

        for (SubmissionItemValueJpaEntity existingValue
                : itemValues) {
            existingByBoxItemId.put(
                    existingValue.getSubmissionBoxItemId(),
                    existingValue
            );
        }

        /*
         * 최종 제출 결과에 포함된 제출 항목 ID입니다.
         * 여기에 없는 기존 항목은 아래에서 제거합니다.
         */
        Set<Long> incomingItemIds =
                submission.getItemValues()
                        .stream()
                        .map(
                                SubmissionItemValue
                                        ::getSubmissionBoxItemId
                        )
                        .collect(Collectors.toSet());

        /*
         * 최종 제출 결과에서 빠진 기존 값을 제거합니다.
         * orphanRemoval=true이므로 DB에서도 삭제됩니다.
         */
        itemValues.removeIf(
                existingValue ->
                        !incomingItemIds.contains(
                                existingValue
                                        .getSubmissionBoxItemId()
                        )
        );

        for (SubmissionItemValue value
                : submission.getItemValues()) {
            SubmissionItemValueJpaEntity existingValue =
                    existingByBoxItemId.get(
                            value.getSubmissionBoxItemId()
                    );

            if (existingValue != null) {
                /*
                 * 파일이나 링크가 교체되더라도 기존 자식 행을
                 * 유지하면서 값만 변경합니다.
                 *
                 * 이렇게 해야 동일 제출 항목에 대한
                 * DELETE/INSERT 순서 충돌을 방지할 수 있습니다.
                 */
                existingValue.updateFrom(value);
                continue;
            }

            /*
             * 이전에 제출하지 않았던 선택 항목을 새로 제출한
             * 경우에만 새로운 자식 엔티티를 추가합니다.
             */
            SubmissionItemValueJpaEntity newValue =
                    SubmissionItemValueJpaEntity.from(value);

            addItemValue(newValue);
        }
    }


    public Long getId() {
        return id;
    }
}
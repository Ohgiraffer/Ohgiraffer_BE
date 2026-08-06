package com.ohgiraffer.submission.infrastructure.persistence;

import com.ohgiraffer.global.entity.BaseTimeEntity;
import com.ohgiraffer.submission.domain.model.SubmissionItemValue;
import jakarta.persistence.*;

@Entity
@Table(name = "submission_item_value")
public class SubmissionItemValueJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_item_value_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "submission_id",
            nullable = false
    )
    private SubmissionJpaEntity submission;

    @Column(
            name = "submission_box_item_id",
            nullable = false
    )
    private Long submissionBoxItemId;

    @Column(
            name = "file_key",
            length = 500
    )
    private String fileKey;

    @Column(
            name = "original_file_name",
            length = 255
    )
    private String originalFileName;

    @Column(
            name = "content_type",
            length = 100
    )
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(
            name = "external_url",
            length = 1000
    )
    private String externalUrl;

    protected SubmissionItemValueJpaEntity() {
    }

    private SubmissionItemValueJpaEntity(
            Long id,
            Long submissionBoxItemId,
            String fileKey,
            String originalFileName,
            String contentType,
            Long fileSize,
            String externalUrl
    ) {
        this.id = id;
        this.submissionBoxItemId = submissionBoxItemId;
        this.fileKey = fileKey;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.externalUrl = externalUrl;
    }

    public static SubmissionItemValueJpaEntity from(
            SubmissionItemValue value
    ) {
        return new SubmissionItemValueJpaEntity(
                value.getId(),
                value.getSubmissionBoxItemId(),
                value.getFileKey(),
                value.getOriginalFileName(),
                value.getContentType(),
                value.getFileSize(),
                value.getExternalUrl()
        );
    }

    public void assignSubmission(
            SubmissionJpaEntity submission
    ) {
        this.submission = submission;
    }

    public SubmissionItemValue toDomain() {
        return SubmissionItemValue.restore(
                id,
                submission.getId(),
                submissionBoxItemId,
                fileKey,
                originalFileName,
                contentType,
                fileSize,
                externalUrl,
                getCreatedAt(),
                getUpdatedAt()
        );
    }
}
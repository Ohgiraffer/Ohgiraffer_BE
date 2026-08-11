package com.ohgiraffer.evaluation.infrastructure.persistence;

import com.ohgiraffer.evaluation.domain.model.EvaluationRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "evaluation_record")
public class EvaluationRecordJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evaluation_record_id")
    private Long id;

    @Column(name = "trainee_id", nullable = false)
    private Long traineeId;

    @Column(name = "sheet_link_id", nullable = false)
    private Long sheetLinkId;

    @Column(name = "evaluation_type", length = 50)
    private String evaluationType;

    @Column(name = "item", length = 255)
    private String item;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "sheet_row_key", nullable = false, length = 255)
    private String sheetRowKey;

    @Column(name = "synced_at", nullable = false)
    private Instant syncedAt;

    protected EvaluationRecordJpaEntity() {
    }

    private EvaluationRecordJpaEntity(EvaluationRecord record) {
        this.id = record.getId();
        this.traineeId = record.getTraineeId();
        this.sheetLinkId = record.getSheetLinkId();
        this.evaluationType = record.getEvaluationType();
        this.item = record.getItem();
        this.score = record.getScore();
        this.comment = record.getComment();
        this.sheetRowKey = record.getSheetRowKey();
        this.syncedAt = record.getSyncedAt();
    }

    public static EvaluationRecordJpaEntity from(EvaluationRecord record) {
        return new EvaluationRecordJpaEntity(record);
    }

    public EvaluationRecord toDomain() {
        return EvaluationRecord.restore(
                id, traineeId, sheetLinkId, evaluationType, item,
                score, comment, sheetRowKey, syncedAt
        );
    }
}

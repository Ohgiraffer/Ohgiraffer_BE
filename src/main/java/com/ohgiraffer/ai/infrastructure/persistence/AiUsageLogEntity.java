package com.ohgiraffer.ai.infrastructure.persistence;

import com.ohgiraffer.ai.domain.model.AiUsageLog;
import com.ohgiraffer.ai.domain.model.FailReason;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_usage_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiUsageLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "feature_name", nullable = false, length = 50)
    private String featureName;

    @Column(name = "model_name", nullable = false, length = 50)
    private String modelName;

    @Column(name = "input_tokens", nullable = false)
    private int inputTokens;

    @Column(name = "output_tokens", nullable = false)
    private int outputTokens;

    @Column(name = "cached_tokens", nullable = false)
    private int cachedTokens;

    @Column(name = "total_tokens", nullable = false)
    private int totalTokens;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Enumerated(EnumType.STRING)
    @Column(name = "fail_reason", length = 30)
    private FailReason failReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private AiUsageLogEntity(String featureName, String modelName, int inputTokens, int outputTokens,
                             int cachedTokens, int totalTokens, boolean success, FailReason failReason,
                             LocalDateTime createdAt) {
        this.featureName = featureName;
        this.modelName = modelName;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.cachedTokens = cachedTokens;
        this.totalTokens = totalTokens;
        this.success = success;
        this.failReason = failReason;
        this.createdAt = createdAt;
    }

    public static AiUsageLogEntity fromDomain(AiUsageLog log) {
        return new AiUsageLogEntity(
                log.getFeatureName(), log.getModelName(),
                log.getInputTokens(), log.getOutputTokens(), log.getCachedTokens(), log.getTotalTokens(),
                log.isSuccess(), log.getFailReason(), log.getCreatedAt()
        );
    }

    public AiUsageLog toDomain() {
        return AiUsageLog.reconstruct(
                featureName, modelName, inputTokens, outputTokens, cachedTokens,
                success, failReason, createdAt
        );
    }
}
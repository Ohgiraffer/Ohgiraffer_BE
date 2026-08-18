package com.ohgiraffer.ai.domain.model;

import java.time.LocalDateTime;

public class AiUsageLog {

    private final String featureName;
    private final String modelName;
    private final int inputTokens;
    private final int outputTokens;
    private final int cachedTokens;
    private final int totalTokens;
    private final boolean success;
    private final FailReason failReason;
    private final LocalDateTime createdAt;

    private AiUsageLog(String featureName, String modelName, int inputTokens, int outputTokens,
                       int cachedTokens, boolean success, FailReason failReason, LocalDateTime createdAt) {
        this.featureName = featureName;
        this.modelName = modelName;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.cachedTokens = cachedTokens;
        this.totalTokens = inputTokens + outputTokens;
        this.success = success;
        this.failReason = failReason;
        this.createdAt = createdAt;
    }

    public static AiUsageLog success(String featureName, String modelName,
                                     int inputTokens, int outputTokens, int cachedTokens) {
        return new AiUsageLog(featureName, modelName, inputTokens, outputTokens, cachedTokens,
                true, null, LocalDateTime.now());
    }

    public static AiUsageLog failure(String featureName, String modelName, FailReason failReason) {
        return new AiUsageLog(featureName, modelName, 0, 0, 0, false, failReason, LocalDateTime.now());
    }

    public static AiUsageLog reconstruct(String featureName, String modelName, int inputTokens,
                                         int outputTokens, int cachedTokens, boolean success,
                                         FailReason failReason, LocalDateTime createdAt) {
        return new AiUsageLog(featureName, modelName, inputTokens, outputTokens, cachedTokens,
                success, failReason, createdAt);
    }

    public String getFeatureName() { return featureName; }
    public String getModelName() { return modelName; }
    public int getInputTokens() { return inputTokens; }
    public int getOutputTokens() { return outputTokens; }
    public int getCachedTokens() { return cachedTokens; }
    public int getTotalTokens() { return totalTokens; }
    public boolean isSuccess() { return success; }
    public FailReason getFailReason() { return failReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
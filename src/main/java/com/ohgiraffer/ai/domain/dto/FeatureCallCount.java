package com.ohgiraffer.ai.domain.dto;

public record  FeatureCallCount(
        String featureName,
        long successCount,
        long failCount,
        long totalTokens
) {}
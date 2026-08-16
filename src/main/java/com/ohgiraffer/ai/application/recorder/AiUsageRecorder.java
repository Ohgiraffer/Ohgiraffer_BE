package com.ohgiraffer.ai.application.recorder;

import com.ohgiraffer.ai.domain.model.AiUsageLog;
import com.ohgiraffer.ai.domain.model.FailReason;
import com.ohgiraffer.ai.domain.repository.AiUsageLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiUsageRecorder {

    private final AiUsageLogRepository aiUsageLogRepository;

    public void recordSuccess(String featureName, String modelName,
                              int inputTokens, int outputTokens, int cachedTokens) {
        try {
            aiUsageLogRepository.save(
                    AiUsageLog.success(featureName, modelName, inputTokens, outputTokens, cachedTokens)
            );
        } catch (Exception e) {
            log.warn("AI usage log 저장 실패: feature={}", featureName, e);
        }
    }

    public void recordFailure(String featureName, String modelName, FailReason failReason) {
        try {
            aiUsageLogRepository.save(AiUsageLog.failure(featureName, modelName, failReason));
        } catch (Exception e) {
            log.warn("AI usage failure log 저장 실패: feature={}", featureName, e);
        }
    }
}
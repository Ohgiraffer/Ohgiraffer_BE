package com.ohgiraffer.ai.domain.repository;

import com.ohgiraffer.ai.domain.model.AiUsageLog;

public interface AiUsageLogRepository {
    void save(AiUsageLog aiUsageLog);
}

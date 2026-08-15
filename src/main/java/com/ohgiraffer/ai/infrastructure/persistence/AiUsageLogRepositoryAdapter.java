package com.ohgiraffer.ai.infrastructure.persistence;

import com.ohgiraffer.ai.domain.dto.AiUsageLastCall;
import com.ohgiraffer.ai.domain.model.AiUsageLog;
import com.ohgiraffer.ai.domain.repository.AiUsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AiUsageLogRepositoryAdapter implements AiUsageLogRepository {

    private final SpringDataAiUsageLogJpaRepository aiUsageLogJpaRepository;

    @Override
    public void save(AiUsageLog aiUsageLog) {
        aiUsageLogJpaRepository.save(AiUsageLogEntity.fromDomain(aiUsageLog));
    }
}
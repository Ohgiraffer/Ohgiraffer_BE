package com.ohgiraffer.aiops.infrastructure.adapter;

import com.ohgiraffer.aiops.domain.model.AgentReasoningLog;
import com.ohgiraffer.aiops.domain.repository.AgentReasoningLogRepository;
import com.ohgiraffer.aiops.infrastructure.persistence.AgentReasoningLogJpaEntity;
import com.ohgiraffer.aiops.infrastructure.persistence.AgentReasoningLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/*
 * comment.
 *  domain.repository.AgentReasoningLogRepository(포트)의 구현체
 *  Domain <-> JpaEntity 변환을 여기서 전담 - application 계층은 Domain 객체만 다룸
 */

@Repository
@RequiredArgsConstructor
public class AgentReasoningLogRepositoryAdapter implements AgentReasoningLogRepository {

    private final AgentReasoningLogJpaRepository agentReasoningLogJpaRepository;

    @Override
    public AgentReasoningLog save(AgentReasoningLog agentReasoningLog) {
        AgentReasoningLogJpaEntity savedEntity =
                agentReasoningLogJpaRepository.save(
                        AgentReasoningLogJpaEntity.from(agentReasoningLog)
                );

        return savedEntity.toDomain();
    }

    @Override
    public List<AgentReasoningLog> findBySessionId(String sessionId) {
        return agentReasoningLogJpaRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream()
                .map(AgentReasoningLogJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<AgentReasoningLog> findByCreatedAtBetween(Instant startAt, Instant endAt) {
        return agentReasoningLogJpaRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startAt, endAt)
                .stream()
                .map(AgentReasoningLogJpaEntity::toDomain)
                .toList();
    }

}

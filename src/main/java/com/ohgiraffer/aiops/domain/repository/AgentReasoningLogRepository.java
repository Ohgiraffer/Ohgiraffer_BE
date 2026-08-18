package com.ohgiraffer.aiops.domain.repository;

import com.ohgiraffer.aiops.domain.model.AgentReasoningLog;

import java.time.Instant;
import java.util.List;

/*
 * comment.
 *  application 계층이 의존하는 포트 - Domain 객체만 주고받고 JPA는 모름
 *  구현체(AgentReasoningLogRepositoryAdapter)는 infrastructure.persistence에 위치
 */

public interface AgentReasoningLogRepository {

    AgentReasoningLog save(AgentReasoningLog agentReasoningLog);

    List<AgentReasoningLog> findBySessionId(String sessionId);

    List<AgentReasoningLog> findByCreatedAtBetween(Instant startAt, Instant endAt);

}

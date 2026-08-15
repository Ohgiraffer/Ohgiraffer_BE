package com.ohgiraffer.aiops.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

/*
 * comment.
 *  Spring Data JPA가 구현체를 자동으로 생성
 *  -> Domain을 모르고 JpaEntity만 다룸
 */

public interface AgentReasoningLogJpaRepository extends JpaRepository<AgentReasoningLogJpaEntity, Long> {

    // 세션별 시간순 조회 - Spring Data 이름 기반 자동구현
    List<AgentReasoningLogJpaEntity> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    // 기간별 최신순 조회 - Spring Data 이름 기반 자동구현
    List<AgentReasoningLogJpaEntity> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant startAt, Instant endAt);

}

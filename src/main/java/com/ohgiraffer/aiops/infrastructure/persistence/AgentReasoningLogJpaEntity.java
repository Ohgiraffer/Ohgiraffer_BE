package com.ohgiraffer.aiops.infrastructure.persistence;

import com.ohgiraffer.aiops.domain.model.AgentReasoningLog;
import com.ohgiraffer.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;

/*
 * comment.
 *  DB 테이블(agent_reasoning_log)과 1:1 매핑되는 JPA 클래스
 *  -> Domain Model(AgentReasoningLog)을 모르고 DB 컬럼 구조만 표현
 *  -> 변환은 AgentReasoningLogRepositoryAdapter가 담당
 *  -> created_at/updated_at은 BaseTimeEntity(AuditingEntityListener)가 자동 관리
 */

@Getter
@Entity
@Table(name = "agent_reasoning_log")
public class AgentReasoningLogJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(name = "turn_id", nullable = false, length = 100)
    private String turnId;

    @Column(name = "function_name", nullable = false, length = 200)
    private String functionName;

    @Column(name = "reasoning_summary", columnDefinition = "TEXT")
    private String reasoningSummary;

    @Column(name = "function_call_id", length = 100)
    private String functionCallId;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "latency_ms")
    private Long latencyMs;

    // Domain -> JpaEntity 변환 (저장용) - createdAt/updatedAt은 BaseTimeEntity가 저장 시 채우므로 여기서 다루지 않음
    public static AgentReasoningLogJpaEntity from(AgentReasoningLog domain) {
        AgentReasoningLogJpaEntity entity = new AgentReasoningLogJpaEntity();
        entity.id = domain.getId();
        entity.sessionId = domain.getSessionId();
        entity.turnId = domain.getTurnId();
        entity.functionName = domain.getFunctionName();
        entity.reasoningSummary = domain.getReasoningSummary();
        entity.functionCallId = domain.getFunctionCallId();
        entity.success = domain.isSuccess();
        entity.latencyMs = domain.getLatencyMs();
        return entity;
    }

    // JpaEntity -> Domain 변환 (조회용) - BaseTimeEntity의 getCreatedAt()/getUpdatedAt() 사용
    public AgentReasoningLog toDomain() {
        return AgentReasoningLog.reconstitute(
                id, sessionId, turnId, functionName, reasoningSummary,
                functionCallId, success, latencyMs, getCreatedAt(), getUpdatedAt()
        );
    }

}

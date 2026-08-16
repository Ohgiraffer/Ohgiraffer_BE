package com.ohgiraffer.aiops.domain.model;

import java.time.Instant;

/*
 * comment.
 *  AI 에이전트 판단 근거(reasoning_summary) 감사 기록 도메인 모델
 *  JPA를 모르는 순수 객체 - 영속성 표현(AgentReasoningLogJpaEntity)과 분리됨
 *  -> 변환은 AgentReasoningLogRepositoryAdapter가 담당
 *  -> createdAt/updatedAt은 BaseTimeEntity가 자동 관리 (create() 시점엔 null, 저장 후 reconstitute()로 채워짐)
 *  함수 호출이 실제로 트리거된 턴만 기록 (매 턴 저장하지 않음)
 */

public class AgentReasoningLog {

    private final Long id;
    private final String sessionId;
    private final String turnId;
    private final String functionName;
    private final String reasoningSummary;
    private final String functionCallId;
    private final boolean success;
    private final Long latencyMs;
    private final Instant createdAt;
    private final Instant updatedAt;

    private AgentReasoningLog(Long id, String sessionId, String turnId, String functionName,
                              String reasoningSummary, String functionCallId, boolean success,
                              Long latencyMs, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.turnId = turnId;
        this.functionName = functionName;
        this.reasoningSummary = reasoningSummary;
        this.functionCallId = functionCallId;
        this.success = success;
        this.latencyMs = latencyMs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 신규 기록 생성 (함수 호출이 트리거된 시점, id/createdAt/updatedAt은 아직 없음 - BaseTimeEntity가 저장 시 자동 채움)
    public static AgentReasoningLog create(String sessionId, String turnId, String functionName,
                                           String reasoningSummary, String functionCallId, boolean success,
                                           Long latencyMs) {
        return new AgentReasoningLog(
                null, sessionId, turnId, functionName, reasoningSummary,
                functionCallId, success, latencyMs, null, null
        );
    }

    // DB에서 조회한 값으로 도메인 객체 복원 (JpaEntity.toDomain()에서 사용)
    public static AgentReasoningLog reconstitute(Long id, String sessionId, String turnId, String functionName,
                                                 String reasoningSummary, String functionCallId, boolean success,
                                                 Long latencyMs, Instant createdAt, Instant updatedAt) {
        return new AgentReasoningLog(
                id, sessionId, turnId, functionName, reasoningSummary,
                functionCallId, success, latencyMs, createdAt, updatedAt
        );
    }

    public Long getId() { return id; }
    public String getSessionId() { return sessionId; }
    public String getTurnId() { return turnId; }
    public String getFunctionName() { return functionName; }
    public String getReasoningSummary() { return reasoningSummary; }
    public String getFunctionCallId() { return functionCallId; }
    public boolean isSuccess() { return success; }
    public Long getLatencyMs() { return latencyMs; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

}

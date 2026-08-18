package com.ohgiraffer.aiops.application.service;

import com.ohgiraffer.aiops.application.command.SaveAgentReasoningLogCommand;
import com.ohgiraffer.aiops.application.usecase.SaveAgentReasoningLogUseCase;
import com.ohgiraffer.aiops.domain.model.AgentReasoningLog;
import com.ohgiraffer.aiops.domain.repository.AgentReasoningLogRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * comment.
 *  createdAt/updatedAt은 BaseTimeEntity(AuditingEntityListener)가 저장 시 자동으로 채우므로
 *  이 서비스에서 Clock을 별도로 다룰 필요가 없다 (ChatMessageMirror 등과 동일한 방식).
 */

@Service
@RequiredArgsConstructor
public class AgentReasoningLogService implements SaveAgentReasoningLogUseCase {

    private final AgentReasoningLogRepository agentReasoningLogRepository;

    @Override
    @Transactional
    public void saveReasoningLog(
            SaveAgentReasoningLogCommand command
    ) {
        validate(command);

        AgentReasoningLog agentReasoningLog =
                AgentReasoningLog.create(
                        command.sessionId(),
                        command.turnId(),
                        command.functionName(),
                        command.reasoningSummary(),
                        command.functionCallId(),
                        command.success(),
                        command.latencyMs()
                );

        agentReasoningLogRepository.save(agentReasoningLog);
    }

    private void validate(
            SaveAgentReasoningLogCommand command
    ) {
        if (isBlank(command.sessionId())
                || isBlank(command.turnId())
                || isBlank(command.functionName())) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "sessionId, turnId, functionName은 필수입니다."
            );
        }
    }

    private boolean isBlank(
            String value
    ) {
        return value == null || value.isBlank();
    }

}

package com.ohgiraffer.aiops.presentation.api.request;

import com.ohgiraffer.aiops.application.command.SaveAgentReasoningLogCommand;
import jakarta.validation.constraints.NotBlank;

public record SaveAgentReasoningLogRequest(
        @NotBlank String sessionId,
        @NotBlank String turnId,
        @NotBlank String functionName,
        String reasoningSummary,
        String functionCallId,
        boolean success,
        Long latencyMs
) {

    public SaveAgentReasoningLogCommand toCommand() {
        return new SaveAgentReasoningLogCommand(
                sessionId,
                turnId,
                functionName,
                reasoningSummary,
                functionCallId,
                success,
                latencyMs
        );
    }

}

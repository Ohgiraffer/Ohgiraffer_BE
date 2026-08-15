package com.ohgiraffer.aiops.presentation.api.request;

import com.ohgiraffer.aiops.application.command.SaveAgentReasoningLogCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record SaveAgentReasoningLogRequest(
        @NotBlank String sessionId,
        @NotBlank String turnId,
        @NotBlank String functionName,
        String reasoningSummary,
        String functionCallId,
        boolean success,
        @PositiveOrZero Long latencyMs
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

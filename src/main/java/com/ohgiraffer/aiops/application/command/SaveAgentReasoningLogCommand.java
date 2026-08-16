package com.ohgiraffer.aiops.application.command;

public record SaveAgentReasoningLogCommand(
        String sessionId,
        String turnId,
        String functionName,
        String reasoningSummary,
        String functionCallId,
        boolean success,
        Long latencyMs
) {
}

package com.ohgiraffer.aiops.application.usecase;

import com.ohgiraffer.aiops.application.command.SaveAgentReasoningLogCommand;

public interface SaveAgentReasoningLogUseCase {

    void saveReasoningLog(
            SaveAgentReasoningLogCommand command
    );

}

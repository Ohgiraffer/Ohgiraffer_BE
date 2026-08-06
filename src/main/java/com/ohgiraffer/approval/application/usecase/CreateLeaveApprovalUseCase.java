package com.ohgiraffer.approval.application.usecase;

import com.ohgiraffer.approval.application.command.CreateLeaveApprovalCommand;

public interface CreateLeaveApprovalUseCase {

    CreateApprovalResult create(
            CreateLeaveApprovalCommand command
    );
}
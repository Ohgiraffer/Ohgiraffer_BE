package com.ohgiraffer.approval.application.usecase;

import com.ohgiraffer.approval.application.command.CreatePurchaseApprovalCommand;

public interface CreatePurchaseApprovalUseCase {

    CreateApprovalResult create(
            CreatePurchaseApprovalCommand command
    );
}
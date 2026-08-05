package com.ohgiraffer.approval.application.usecase;

import com.ohgiraffer.approval.application.command.RegisterSignatureCommand;

public interface RegisterSignatureUseCase {

    SignatureResult register(
            RegisterSignatureCommand command
    );
}
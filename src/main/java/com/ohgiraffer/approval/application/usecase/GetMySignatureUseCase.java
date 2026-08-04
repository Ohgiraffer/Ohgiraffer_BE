package com.ohgiraffer.approval.application.usecase;

public interface GetMySignatureUseCase {

    SignatureResult getMySignature(
            Long userId
    );
}
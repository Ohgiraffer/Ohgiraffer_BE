package com.ohgiraffer.approval.presentation.api.response;

import com.ohgiraffer.approval.application.usecase.SignatureResult;

import java.time.LocalDateTime;

public record SignatureResponse(
        Long signatureId,
        String signatureImage,
        String originalFileName,
        Long fileSizeBytes,
        String fileType,
        boolean active,
        LocalDateTime updatedAt
) {

    public static SignatureResponse from(
            SignatureResult result
    ) {
        return new SignatureResponse(
                result.signatureId(),
                result.signatureImage(),
                result.originalFileName(),
                result.fileSizeBytes(),
                result.fileType(),
                result.active(),
                result.updatedAt()
        );
    }
}
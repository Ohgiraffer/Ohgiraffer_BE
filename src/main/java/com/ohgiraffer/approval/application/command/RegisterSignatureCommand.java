package com.ohgiraffer.approval.application.command;

public record RegisterSignatureCommand(
        Long userId,
        byte[] signatureImage,
        String originalFileName,
        Long fileSizeBytes,
        String fileType
) {
}
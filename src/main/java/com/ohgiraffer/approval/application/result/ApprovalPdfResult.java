package com.ohgiraffer.approval.application.result;

public record ApprovalPdfResult(
        String fileName,
        byte[] content
) {
}
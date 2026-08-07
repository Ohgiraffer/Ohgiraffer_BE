package com.ohgiraffer.submission.application.usecase;

public record PreviewSubmissionFileResult(
        Long submissionItemValueId,
        String originalFileName,
        String contentType,
        Long fileSize,
        String previewUrl
) {
}
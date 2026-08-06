package com.ohgiraffer.submission.application.usecase;

public record DownloadSubmissionFileResult(
        Long submissionItemValueId,
        String originalFileName,
        String contentType,
        Long fileSize,
        String downloadUrl
) {
}
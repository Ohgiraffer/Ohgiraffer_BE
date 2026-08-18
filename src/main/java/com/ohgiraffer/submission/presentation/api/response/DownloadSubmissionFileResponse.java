package com.ohgiraffer.submission.presentation.api.response;

import com.ohgiraffer.submission.application.usecase.DownloadSubmissionFileResult;

public record DownloadSubmissionFileResponse(
        Long submissionItemValueId,
        String originalFileName,
        String contentType,
        Long fileSize,
        String downloadUrl
) {

    public static DownloadSubmissionFileResponse from(
            DownloadSubmissionFileResult result
    ) {
        return new DownloadSubmissionFileResponse(
                result.submissionItemValueId(),
                result.originalFileName(),
                result.contentType(),
                result.fileSize(),
                result.downloadUrl()
        );
    }
}
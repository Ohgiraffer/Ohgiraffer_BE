package com.ohgiraffer.submission.presentation.api.response;

import com.ohgiraffer.submission.application.usecase.PreviewSubmissionFileResult;

public record PreviewSubmissionFileResponse(
        Long submissionItemValueId,
        String originalFileName,
        String contentType,
        Long fileSize,
        String previewUrl
) {

    public static PreviewSubmissionFileResponse from(
            PreviewSubmissionFileResult result
    ) {
        return new PreviewSubmissionFileResponse(
                result.submissionItemValueId(),
                result.originalFileName(),
                result.contentType(),
                result.fileSize(),
                result.previewUrl()
        );
    }
}
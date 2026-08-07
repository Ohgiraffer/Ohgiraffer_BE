package com.ohgiraffer.survey.application.usecase;

public record SurveySummaryPdfResult(
        String fileName,
        byte[] content
) {

    public SurveySummaryPdfResult {
        if (fileName == null
                || fileName.isBlank()) {
            throw new IllegalArgumentException(
                    "PDF 파일명이 필요합니다."
            );
        }

        if (content == null
                || content.length == 0) {
            throw new IllegalArgumentException(
                    "PDF 파일 내용이 필요합니다."
            );
        }

        content = content.clone();
    }

    @Override
    public byte[] content() {
        return content.clone();
    }
}
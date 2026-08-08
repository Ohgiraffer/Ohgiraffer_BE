package com.ohgiraffer.notice.presentation.api.response;

import com.ohgiraffer.notice.application.usecase.NoticeAttachmentCommandUseCase.UploadedNoticeAttachment;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 공지 등록 전에 올려 둔 첨부파일.
 *
 * <p>아직 공지에 이어지지 않아 식별자가 없다. {@code fileKey} 를 공지 등록 요청에
 * 그대로 실어 보내면 그때 이어진다.
 */
@Schema(description = "공지 등록 전 올린 첨부파일")
public record UploadedNoticeAttachmentResponse(

        @Schema(
                description = """
                        저장소 키. 공지 등록 요청의 attachments 에 그대로 넣어 보낸다.
                        """,
                example = "noticeAttachments/3f2504e0-4f89-41d3-9a0c-0305e82c3301.pdf"
        )
        String fileKey,

        @Schema(description = "원본 파일명", example = "8월 휴강 안내.pdf")
        String fileName,

        @Schema(description = "파일 크기 (바이트)", example = "204800")
        Long fileSizeBytes,

        @Schema(description = "파일 형식", example = "application/pdf")
        String fileType
) {

    public static UploadedNoticeAttachmentResponse from(
            UploadedNoticeAttachment uploaded
    ) {
        return new UploadedNoticeAttachmentResponse(
                uploaded.fileKey(),
                uploaded.fileName(),
                uploaded.fileSizeBytes(),
                uploaded.fileType()
        );
    }
}

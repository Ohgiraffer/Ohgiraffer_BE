package com.ohgiraffer.notice.presentation.api.response;

import com.ohgiraffer.notice.domain.model.NoticeAttachment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 공지 첨부파일 하나.
 *
 * <p>{@code downloadUrl} 은 저장된 값이 아니라 조회할 때마다 새로 만든 주소다.
 * 만료 시간이 있어 저장해 두면 곧 쓸모없어진다.
 */
@Schema(description = "공지 첨부파일")
public record NoticeAttachmentResponse(

        @Schema(description = "첨부파일 식별자", example = "1")
        Long noticeAttachmentId,

        @Schema(description = "원본 파일명", example = "8월 휴강 안내.pdf")
        String fileName,

        @Schema(description = "파일 크기 (바이트)", example = "204800")
        Long fileSizeBytes,

        @Schema(description = "파일 형식", example = "application/pdf")
        String fileType,

        @Schema(
                description = """
                        다운로드 주소. 발급 시점부터 5분간 유효하다.
                        만료되면 공지 상세를 다시 조회하면 된다.
                        """
        )
        String downloadUrl,

        @Schema(description = "업로드일시 (KST)", example = "2026-08-07T14:20:00")
        LocalDateTime uploadedAt
) {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static NoticeAttachmentResponse from(
            NoticeAttachment attachment,
            String downloadUrl
    ) {
        return new NoticeAttachmentResponse(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getFileSizeBytes(),
                attachment.getFileType(),
                downloadUrl,
                toKst(attachment.getUploadedAt())
        );
    }

    private static LocalDateTime toKst(Instant instant) {
        if (instant == null) {
            return null;
        }

        return instant.atZone(KST).toLocalDateTime();
    }
}

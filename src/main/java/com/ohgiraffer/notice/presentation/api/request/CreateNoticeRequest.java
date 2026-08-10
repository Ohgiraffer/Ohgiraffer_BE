package com.ohgiraffer.notice.presentation.api.request;

import com.ohgiraffer.notice.application.command.CreateNoticeCommand;
import com.ohgiraffer.notice.application.command.NoticeAttachmentCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 공지 등록 요청.
 *
 * <p>작성자는 클라이언트가 지정하지 않는다. 서버가 로그인 사용자로 채운다.
 */
public record CreateNoticeRequest(

        @NotNull(message = "공지 카테고리는 필수입니다.")
        Long categoryId,

        @NotBlank(message = "공지 제목은 필수입니다.")
        @Size(max = 255, message = "공지 제목은 255자 이하로 입력해주세요.")
        String title,

        @NotBlank(message = "공지 본문은 필수입니다.")
        String content,

        /*
         * 요구사항상 "고정 여부 (선택)" 이라 생략할 수 있어야 한다.
         * record 는 원시 타입 필드가 JSON 에 없으면 역직렬화 자체가 실패하므로
         * 래퍼 타입으로 받아 null 을 기본값으로 해석한다. visibleToTrainee 도 동일.
         */
        Boolean pinned,

        Boolean visibleToTrainee,

        /*
         * POST /notices/attachments 로 미리 올려 두고 받은 값을 그대로 넣는다.
         * 첨부가 없으면 생략하거나 빈 배열을 보내면 된다.
         */
        @Valid
        List<NoticeAttachmentRequest> attachments
) {

    public CreateNoticeCommand toCommand(Long authorId) {
        return new CreateNoticeCommand(
                authorId,
                categoryId,
                title,
                content,
                pinned != null && pinned,
                visibleToTrainee == null || visibleToTrainee,
                attachments == null
                        ? List.of()
                        : attachments.stream()
                                .map(NoticeAttachmentRequest::toCommand)
                                .toList()
        );
    }

    /**
     * 미리 올려 둔 첨부파일 하나.
     *
     * <p>{@code fileKey} 만이 서버가 실제로 쓰는 값이다. 나머지는 화면에 보여줄 값이라
     * 올릴 때 받은 것을 그대로 돌려주면 된다.
     */
    public record NoticeAttachmentRequest(

            @NotBlank(message = "파일 저장 키는 필수입니다.")
            String fileKey,

            String fileName,

            /*
             * 미리 올릴 때 서버가 돌려준 값을 그대로 실어 보내므로 없을 수 없다.
             * 빠뜨렸을 때 0 으로 채우면 "빈 파일" 이라는 엉뚱한 사유로 거절돼,
             * 화면은 크기를 안 보낸 것이 원인인지 알 수 없다.
             */
            @NotNull(message = "파일 크기는 필수입니다.")
            @Positive(message = "파일 크기는 0보다 커야 합니다.")
            Long fileSizeBytes,

            String fileType
    ) {

        public NoticeAttachmentCommand toCommand() {
            return new NoticeAttachmentCommand(
                    fileKey,
                    fileName,
                    fileSizeBytes,
                    fileType
            );
        }
    }
}

package com.ohgiraffer.notice.presentation.api.request;

import com.ohgiraffer.notice.application.command.CreateNoticeCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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

        Boolean visibleToTrainee
) {

    public CreateNoticeCommand toCommand(Long authorId) {
        return new CreateNoticeCommand(
                authorId,
                categoryId,
                title,
                content,
                pinned != null && pinned,
                visibleToTrainee == null || visibleToTrainee
        );
    }
}

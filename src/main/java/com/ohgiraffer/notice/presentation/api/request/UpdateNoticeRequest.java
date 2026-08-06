package com.ohgiraffer.notice.presentation.api.request;

import com.ohgiraffer.notice.application.command.UpdateNoticeCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 공지 수정 요청.
 *
 * <p>요구사항상 "필수 내용 누락 불가능"이라 등록과 같은 항목을 모두 받아 전체를 교체한다.
 * 작성자는 바꿀 수 없고, 수정 권한 확인에만 로그인 사용자를 쓴다.
 */
public record UpdateNoticeRequest(

        @NotNull(message = "공지 카테고리는 필수입니다.")
        Long categoryId,

        @NotBlank(message = "공지 제목은 필수입니다.")
        @Size(max = 255, message = "공지 제목은 255자 이하로 입력해주세요.")
        String title,

        @NotBlank(message = "공지 본문은 필수입니다.")
        String content,

        Boolean mandatory,

        Boolean visibleToTrainee
) {

    public UpdateNoticeCommand toCommand(Long noticeId, Long editorId) {
        return new UpdateNoticeCommand(
                noticeId,
                editorId,
                categoryId,
                title,
                content,
                mandatory != null && mandatory,
                visibleToTrainee == null || visibleToTrainee
        );
    }
}

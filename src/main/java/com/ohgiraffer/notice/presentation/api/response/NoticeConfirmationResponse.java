package com.ohgiraffer.notice.presentation.api.response;

import com.ohgiraffer.notice.application.query.NoticeConfirmationView;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 공지 확인 처리 응답. 화면은 이 값으로 체크박스와 인원수를 바로 갱신한다.
 *
 * <p>상세 조회 응답의 같은 이름 필드와 뜻이 같으므로 화면에서 그대로 갈아끼우면 된다.
 */
@Schema(description = "공지 확인 처리 결과")
public record NoticeConfirmationResponse(

        @Schema(description = "공지 식별자", example = "1")
        Long noticeId,

        @Schema(description = "확인한 인원 수", example = "12")
        long confirmationCount,

        @Schema(description = "내가 확인했는지 여부", example = "true")
        boolean confirmedByMe
) {

    public static NoticeConfirmationResponse from(NoticeConfirmationView view) {
        return new NoticeConfirmationResponse(
                view.noticeId(),
                view.confirmationCount(),
                view.confirmedByMe()
        );
    }
}

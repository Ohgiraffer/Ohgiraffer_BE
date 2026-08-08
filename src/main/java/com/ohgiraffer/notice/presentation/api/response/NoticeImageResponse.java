package com.ohgiraffer.notice.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 공지 본문 이미지 업로드 결과.
 *
 * <p>첨부파일 응답과 달리 식별자가 없다. 이 이미지를 어디에 쓰는지는 본문이 알고 있어
 * 따로 행을 남기지 않기 때문이다.
 */
@Schema(description = "공지 본문 이미지 업로드 결과")
public record NoticeImageResponse(

        @Schema(
                description = """
                        본문에 그대로 넣을 이미지 주소. 만료되지 않는다.
                        """,
                example = "https://ohgiraffer-media.s3.ap-northeast-2.amazonaws.com/noticeImages/uuid.png"
        )
        String imageUrl
) {
}

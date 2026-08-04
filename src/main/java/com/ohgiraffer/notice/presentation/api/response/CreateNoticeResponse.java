package com.ohgiraffer.notice.presentation.api.response;

import com.ohgiraffer.notice.domain.model.Notice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 공지 등록 결과.
 *
 * <p>시각은 서버에서 한국 시간으로 변환해 내려준다. 저장은 UTC({@code Instant})로 하지만,
 * 응답을 UTC 로 주면 프론트가 날짜만 잘라 쓸 때 오전 9시 이전 공지가 하루 전으로
 * 표시되는 문제가 있어 서버가 변환 책임을 갖는다.
 */
public record CreateNoticeResponse(
        Long noticeId,
        String title,
        boolean mandatory,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static CreateNoticeResponse from(Notice notice) {
        return new CreateNoticeResponse(
                notice.getId(),
                notice.getTitle(),
                notice.isMandatory(),
                toKst(notice.getCreatedAt()),
                toKst(notice.getUpdatedAt())
        );
    }

    private static LocalDateTime toKst(Instant instant) {
        if (instant == null) {
            return null;
        }

        return instant.atZone(KST).toLocalDateTime();
    }
}

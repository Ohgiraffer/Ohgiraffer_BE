package com.ohgiraffer.notice.application.query;

import com.ohgiraffer.notice.domain.model.Notice;

import java.time.Instant;

/**
 * 공지 목록 화면의 한 줄.
 *
 * <p>{@code authorName} 은 사용자 도메인에서 포트로 가져온다.
 * 탈퇴 등으로 사용자를 찾지 못하면 null 이며, 화면에서 대체 문구를 보여주면 된다.
 */
public record NoticeSummaryView(
        Long noticeId,
        Long categoryId,
        String categoryName,
        String title,
        Long authorId,
        String authorName,
        boolean mandatory,
        boolean confirmedByMe,
        Instant createdAt
) {

    public static NoticeSummaryView of(
            Notice notice,
            String categoryName,
            String authorName,
            boolean confirmedByMe
    ) {
        return new NoticeSummaryView(
                notice.getId(),
                notice.getCategoryId(),
                categoryName,
                notice.getTitle(),
                notice.getAuthorId(),
                authorName,
                notice.isMandatory(),
                confirmedByMe,
                notice.getCreatedAt()
        );
    }
}

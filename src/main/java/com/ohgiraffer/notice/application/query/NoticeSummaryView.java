package com.ohgiraffer.notice.application.query;

import com.ohgiraffer.notice.domain.model.Notice;

import java.time.Instant;

/**
 * 공지 목록 화면의 한 줄.
 *
 * <p>요구사항의 목록 항목 중 아직 채우지 못하는 것이 둘 있다.
 * <ul>
 *   <li>작성자 이름 — users 는 인증 도메인 소유라 직접 조회하지 않는다.
 *       사용자 도메인에 조회 유스케이스가 생기면 포트로 채운다.</li>
 *   <li>확인 여부 — 공지 확인 기능을 구현할 때 함께 추가한다.</li>
 * </ul>
 */
public record NoticeSummaryView(
        Long noticeId,
        Long categoryId,
        String categoryName,
        String title,
        Long authorId,
        boolean mandatory,
        Instant createdAt
) {

    public static NoticeSummaryView of(
            Notice notice,
            String categoryName
    ) {
        return new NoticeSummaryView(
                notice.getId(),
                notice.getCategoryId(),
                categoryName,
                notice.getTitle(),
                notice.getAuthorId(),
                notice.isMandatory(),
                notice.getCreatedAt()
        );
    }
}

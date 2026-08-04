package com.ohgiraffer.notice.application.query;

import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.model.NoticeCategory;

import java.time.Instant;

/**
 * 공지 상세 화면용 조회 모델.
 *
 * <p>공지 애그리거트 하나로는 채울 수 없는 값(카테고리명 등)이 섞이므로
 * 도메인 모델 대신 조회 전용 뷰를 둔다.
 *
 * <p>아직 채우지 못하는 항목이 있다.
 * <ul>
 *   <li>작성자 이름 — users 는 인증 도메인 소유라 직접 조회하지 않는다.
 *       사용자 도메인이 들어오면 포트를 통해 authorName 을 채운다.</li>
 *   <li>첨부파일, 확인 인원 수, 본인 확인 여부 — 해당 기능 구현 시점에 추가한다.</li>
 * </ul>
 */
public record NoticeDetailView(
        Long noticeId,
        Long categoryId,
        String categoryName,
        String title,
        String content,
        Long authorId,
        boolean mandatory,
        boolean visibleToTrainee,
        Instant createdAt,
        Instant updatedAt
) {

    public static NoticeDetailView of(
            Notice notice,
            NoticeCategory category
    ) {
        return new NoticeDetailView(
                notice.getId(),
                notice.getCategoryId(),
                category == null ? null : category.getName(),
                notice.getTitle(),
                notice.getContent(),
                notice.getAuthorId(),
                notice.isMandatory(),
                notice.isVisibleToTrainee(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}

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
 * <p>{@code authorName} 은 사용자 도메인에서 포트로 가져온다.
 * 탈퇴 등으로 사용자를 찾지 못하면 null 이며, 화면에서 대체 문구를 보여주면 된다.
 *
 * <p>첨부파일은 아직 없다. 파일 저장소를 붙일 때 추가한다.
 */
public record NoticeDetailView(
        Long noticeId,
        Long categoryId,
        String categoryName,
        String title,
        String content,
        Long authorId,
        String authorName,
        boolean pinned,
        boolean visibleToTrainee,
        long confirmationCount,
        boolean confirmedByMe,
        Instant createdAt,
        Instant updatedAt
) {

    public static NoticeDetailView of(
            Notice notice,
            NoticeCategory category,
            String authorName,
            long confirmationCount,
            boolean confirmedByMe
    ) {
        return new NoticeDetailView(
                notice.getId(),
                notice.getCategoryId(),
                category == null ? null : category.getName(),
                notice.getTitle(),
                notice.getContent(),
                notice.getAuthorId(),
                authorName,
                notice.isPinned(),
                notice.isVisibleToTrainee(),
                confirmationCount,
                confirmedByMe,
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}

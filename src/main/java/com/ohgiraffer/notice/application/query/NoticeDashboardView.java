package com.ohgiraffer.notice.application.query;

import com.ohgiraffer.notice.domain.model.Notice;

import java.time.Instant;

/**
 * 메인 대시보드의 공지 카드 한 장.
 *
 * <p>카드에는 제목·등록일·고정 여부만 나오므로 카테고리명이나 작성자는 담지 않는다.
 * 목록 화면과 달리 곁들이는 정보가 없어 추가 조회 없이 공지 한 번으로 채워진다.
 */
public record NoticeDashboardView(
        Long noticeId,
        String title,
        boolean pinned,
        Instant createdAt
) {

    public static NoticeDashboardView of(Notice notice) {
        return new NoticeDashboardView(
                notice.getId(),
                notice.getTitle(),
                notice.isPinned(),
                notice.getCreatedAt()
        );
    }
}

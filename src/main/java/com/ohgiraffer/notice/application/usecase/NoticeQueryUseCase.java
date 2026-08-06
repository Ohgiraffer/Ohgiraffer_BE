package com.ohgiraffer.notice.application.usecase;

import com.ohgiraffer.notice.application.query.NoticeDetailView;
import com.ohgiraffer.notice.application.query.NoticeSummaryView;
import com.ohgiraffer.notice.domain.model.ViewerRole;

import java.util.List;

/**
 * 공지 조회 유스케이스.
 *
 * <p>확인 여부는 보는 사람마다 다르므로 조회할 때 사용자 식별자를 함께 받는다.
 */
public interface NoticeQueryUseCase {

    NoticeDetailView findDetail(Long noticeId, ViewerRole viewer, Long userId);

    /**
     * 필수 공지 우선, 최신순으로 정렬된 목록을 반환한다.
     *
     * @param categoryId 카테고리 탭 필터. null 이면 전체
     */
    List<NoticeSummaryView> findAll(
            ViewerRole viewer,
            Long categoryId,
            Long userId
    );
}

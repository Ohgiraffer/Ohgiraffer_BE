package com.ohgiraffer.notice.application.usecase;

import com.ohgiraffer.notice.application.query.NoticeDashboardView;
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
     * 고정 공지 우선, 최신순으로 정렬된 목록을 반환한다.
     *
     * @param categoryId 카테고리 탭 필터. null 이면 전체
     */
    List<NoticeSummaryView> findAll(
            ViewerRole viewer,
            Long categoryId,
            Long userId
    );

    /**
     * 메인 대시보드 공지 요약.
     *
     * <p>최근 올라온 고정 공지와 아직 확인하지 않은 공지를 우선순위대로 돌려준다.
     * 5개 단위 페이지네이션과 더보기는 화면에서 처리하기로 해 서버는 자르지 않는다.
     */
    List<NoticeDashboardView> findDashboardSummary(
            ViewerRole viewer,
            Long userId
    );
}

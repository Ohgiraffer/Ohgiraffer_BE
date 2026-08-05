package com.ohgiraffer.notice.domain.repository;

import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.model.ViewerRole;

import java.util.List;
import java.util.Optional;

/**
 * 공지 영속성 포트. 구현은 infrastructure 계층의 어댑터가 담당한다.
 */
public interface NoticeRepository {

    Notice save(Notice notice);

    Optional<Notice> findById(Long noticeId);

    /**
     * 조회자에게 보여줄 수 있는 공지를 필수 공지 우선, 최신순으로 반환한다.
     *
     * <p>페이지네이션은 화면에서 처리하기로 해 전체를 돌려준다.
     *
     * @param viewer     훈련생이면 훈련생 비공개 공지를 제외한다
     * @param categoryId 카테고리 탭 필터. null 이면 전체 카테고리
     */
    List<Notice> findAllVisible(ViewerRole viewer, Long categoryId);
}

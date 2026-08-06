package com.ohgiraffer.notice.domain.repository;

import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.model.ViewerRole;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 공지 영속성 포트. 구현은 infrastructure 계층의 어댑터가 담당한다.
 */
public interface NoticeRepository {

    Notice save(Notice notice);

    /**
     * 이미 저장된 공지의 내용을 바꾼다.
     *
     * <p>{@link #save}로 처리하지 않는 이유는, 도메인 모델에는 생성 시각이 없어
     * 그대로 저장하면 created_at 이 비워지기 때문이다. 저장된 것을 읽어 값만 덮어쓴다.
     */
    Notice update(Notice notice);

    void deleteById(Long noticeId);

    Optional<Notice> findById(Long noticeId);

    /**
     * 조회자에게 보여줄 수 있는 공지를 고정 공지 우선, 최신순으로 반환한다.
     *
     * <p>페이지네이션은 화면에서 처리하기로 해 전체를 돌려준다.
     *
     * @param viewer     훈련생이면 훈련생 비공개 공지를 제외한다
     * @param categoryId 카테고리 탭 필터. null 이면 전체 카테고리
     */
    List<Notice> findAllVisible(ViewerRole viewer, Long categoryId);

    /**
     * 메인 대시보드 공지 요약 대상.
     *
     * <p>최근 올라온 고정 공지, 그리고 아직 확인하지 않은 공지를 함께 돌려준다.
     * 고정 공지가 먼저 오고 같은 등급 안에서는 최신순이다.
     *
     * @param since 고정 공지를 "최근"으로 볼 기준 시각
     */
    List<Notice> findDashboardSummary(
            ViewerRole viewer,
            Long userId,
            Instant since
    );

    /**
     * 해당 카테고리를 쓰는 공지 수.
     *
     * <p>카테고리 삭제 전에 확인한다. notice.notice_category_id 외래키에 ON DELETE 절이 없어
     * 사용 중인 카테고리를 지우면 DB가 제약 위반을 던지고 500 으로 새기 때문에,
     * 미리 세어 보고 몇 건이 막고 있는지 알려준다.
     */
    long countByCategoryId(Long categoryId);
}

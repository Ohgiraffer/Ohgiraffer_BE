package com.ohgiraffer.notice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SpringDataNoticeRepository
        extends JpaRepository<NoticeJpaEntity, Long> {

    /**
     * 두 조건 모두 null 이면 필터를 적용하지 않는다.
     *
     * <p>정렬은 "고정 공지는 일반 공지보다 상단에 우선 표시" 요구사항을 따르고,
     * 같은 등급 안에서는 최신 공지가 위로 온다.
     *
     * @param visibleToTrainee null 이면 공개 여부를 가리지 않는다
     */
    @Query("""
            select n
              from NoticeJpaEntity n
             where (:categoryId is null or n.categoryId = :categoryId)
               and (:visibleToTrainee is null or n.visibleToTrainee = :visibleToTrainee)
             order by n.pinned desc, n.createdAt desc
            """)
    List<NoticeJpaEntity> findAllVisible(
            @Param("categoryId") Long categoryId,
            @Param("visibleToTrainee") Boolean visibleToTrainee
    );

    /**
     * 메인 대시보드 공지 요약. 사용자가 우선 확인해야 할 공지만 골라 반환한다.
     *
     * <p>대상은 두 가지를 합친 것이다.
     * <ul>
     *   <li>최근 올라온 고정 공지 — 이미 확인했더라도 잠시 눈에 띄게 둔다</li>
     *   <li>아직 확인하지 않은 공지 — 고정이든 일반이든 가리지 않는다</li>
     * </ul>
     *
     * <p>확인 여부를 애플리케이션에서 거르지 않고 {@code not exists} 로 함께 묻는 이유는,
     * 그래야 대시보드에 필요한 것만 한 번의 질의로 가져올 수 있기 때문이다.
     * 전체를 읽어와 걸러내면 공지가 쌓일수록 버리는 양만 늘어난다.
     *
     * @param visibleToTrainee null 이면 공개 여부를 가리지 않는다
     * @param since            고정 공지를 최근으로 볼 기준 시각
     */
    @Query("""
            select n
              from NoticeJpaEntity n
             where (:visibleToTrainee is null or n.visibleToTrainee = :visibleToTrainee)
               and (
                     (n.pinned = true and n.createdAt >= :since)
                     or not exists (
                            select c.noticeId
                              from NoticeConfirmationJpaEntity c
                             where c.noticeId = n.id
                               and c.userId = :userId
                     )
                   )
             order by n.pinned desc, n.createdAt desc
            """)
    List<NoticeJpaEntity> findDashboardSummary(
            @Param("visibleToTrainee") Boolean visibleToTrainee,
            @Param("userId") Long userId,
            @Param("since") Instant since
    );

    long countByCategoryId(Long categoryId);
}

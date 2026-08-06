package com.ohgiraffer.notice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataNoticeRepository
        extends JpaRepository<NoticeJpaEntity, Long> {

    /**
     * 두 조건 모두 null 이면 필터를 적용하지 않는다.
     *
     * <p>정렬은 "필수 공지는 일반 공지보다 상단에 우선 표시" 요구사항을 따르고,
     * 같은 등급 안에서는 최신 공지가 위로 온다.
     *
     * @param visibleToTrainee null 이면 공개 여부를 가리지 않는다
     */
    @Query("""
            select n
              from NoticeJpaEntity n
             where (:categoryId is null or n.categoryId = :categoryId)
               and (:visibleToTrainee is null or n.visibleToTrainee = :visibleToTrainee)
             order by n.mandatory desc, n.createdAt desc
            """)
    List<NoticeJpaEntity> findAllVisible(
            @Param("categoryId") Long categoryId,
            @Param("visibleToTrainee") Boolean visibleToTrainee
    );

    long countByCategoryId(Long categoryId);
}

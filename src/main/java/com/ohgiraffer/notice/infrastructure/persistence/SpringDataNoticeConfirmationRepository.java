package com.ohgiraffer.notice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SpringDataNoticeConfirmationRepository
        extends JpaRepository<NoticeConfirmationJpaEntity, NoticeConfirmationId> {

    long countByNoticeId(Long noticeId);

    @Query("""
            select c.noticeId
              from NoticeConfirmationJpaEntity c
             where c.userId = :userId
               and c.noticeId in :noticeIds
            """)
    List<Long> findConfirmedNoticeIds(
            @Param("userId") Long userId,
            @Param("noticeIds") Collection<Long> noticeIds
    );
}

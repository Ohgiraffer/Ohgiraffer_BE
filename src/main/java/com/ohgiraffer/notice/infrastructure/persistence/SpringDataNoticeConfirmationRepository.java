package com.ohgiraffer.notice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SpringDataNoticeConfirmationRepository
        extends JpaRepository<NoticeConfirmationJpaEntity, NoticeConfirmationId> {

    /**
     * 확인 기록을 한 문장으로 남긴다. 이미 있으면 아무것도 바꾸지 않는다.
     *
     * <p>존재 여부를 먼저 묻고 저장하면 그 사이에 같은 사용자의 두 번째 요청이 끼어들 수 있다.
     * 체크박스를 빠르게 두 번 누르면 실제로 일어나고, 뒤늦은 쪽이 복합 PK 중복으로 터진다.
     * INSERT 한 문장이면 DB가 알아서 가려내므로 그 틈이 없다.
     *
     * <p>중복일 때 {@code notice_id = notice_id} 로 두는 것은 아무것도 바꾸지 않기 위해서다.
     * confirmed_at 을 건드리면 다시 누를 때마다 최초 확인 시각이 밀린다.
     *
     * <p>confirmed_at 은 넘기지 않고 컬럼 기본값(CURRENT_TIMESTAMP)에 맡긴다.
     */
    @Modifying
    @Query(
            value = """
                    insert into notice_confirmation (notice_id, user_id)
                    values (:noticeId, :userId)
                    on duplicate key update notice_id = notice_id
                    """,
            nativeQuery = true
    )
    void insertIfAbsent(
            @Param("noticeId") Long noticeId,
            @Param("userId") Long userId
    );

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

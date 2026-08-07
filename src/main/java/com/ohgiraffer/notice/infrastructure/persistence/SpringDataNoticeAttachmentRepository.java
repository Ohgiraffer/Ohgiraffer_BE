package com.ohgiraffer.notice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataNoticeAttachmentRepository
        extends JpaRepository<NoticeAttachmentJpaEntity, Long> {

    /**
     * 첨부는 올린 순서대로 보여준다. 화면에 나열 기준이 따로 없어 등록순이 가장 자연스럽다.
     */
    List<NoticeAttachmentJpaEntity> findAllByNoticeIdOrderByIdAsc(Long noticeId);

    long countByNoticeId(Long noticeId);
}

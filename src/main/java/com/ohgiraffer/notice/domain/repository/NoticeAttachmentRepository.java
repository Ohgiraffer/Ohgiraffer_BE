package com.ohgiraffer.notice.domain.repository;

import com.ohgiraffer.notice.domain.model.NoticeAttachment;

import java.util.List;
import java.util.Optional;

/**
 * 공지 첨부파일 영속성 포트. 구현은 infrastructure 계층의 어댑터가 담당한다.
 */
public interface NoticeAttachmentRepository {

    List<NoticeAttachment> saveAll(List<NoticeAttachment> attachments);

    List<NoticeAttachment> findAllByNoticeId(Long noticeId);

    Optional<NoticeAttachment> findById(Long noticeAttachmentId);

    void deleteById(Long noticeAttachmentId);

    /**
     * 공지에 이미 붙어 있는 첨부 수. 개수 상한을 확인할 때 쓴다.
     */
    long countByNoticeId(Long noticeId);
}

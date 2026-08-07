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

    /**
     * 이미 어떤 공지가 쓰고 있는 저장 키인지 여부.
     *
     * <p>등록 요청의 저장 키는 클라이언트를 거쳐 들어오므로 같은 키가 두 공지에 붙을 수 있다.
     * 그러면 한쪽 공지를 지울 때 저장소 객체가 사라져 다른 공지의 첨부가 깨진다.
     */
    boolean existsByFileKey(String fileKey);
}

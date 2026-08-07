package com.ohgiraffer.notice.infrastructure.persistence;

import com.ohgiraffer.notice.domain.model.NoticeAttachment;
import com.ohgiraffer.notice.domain.repository.NoticeAttachmentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class NoticeAttachmentRepositoryAdapter
        implements NoticeAttachmentRepository {

    private final SpringDataNoticeAttachmentRepository
            springDataNoticeAttachmentRepository;

    public NoticeAttachmentRepositoryAdapter(
            SpringDataNoticeAttachmentRepository springDataNoticeAttachmentRepository
    ) {
        this.springDataNoticeAttachmentRepository =
                springDataNoticeAttachmentRepository;
    }

    @Override
    public List<NoticeAttachment> saveAll(List<NoticeAttachment> attachments) {
        List<NoticeAttachmentJpaEntity> entities = attachments.stream()
                .map(NoticeAttachmentJpaEntity::from)
                .toList();

        /*
         * saveAll 이 아니라 saveAllAndFlush 를 쓴다. 저장을 미루면 공지가 사이에 지워졌을 때
         * 외래키 위반이 이 메서드 밖에서 터져, S3 에 올린 파일을 되돌릴 기회를 놓친다.
         */
        return springDataNoticeAttachmentRepository.saveAllAndFlush(entities)
                .stream()
                .map(NoticeAttachmentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<NoticeAttachment> findAllByNoticeId(Long noticeId) {
        return springDataNoticeAttachmentRepository
                .findAllByNoticeIdOrderByIdAsc(noticeId)
                .stream()
                .map(NoticeAttachmentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<NoticeAttachment> findById(Long noticeAttachmentId) {
        return springDataNoticeAttachmentRepository.findById(noticeAttachmentId)
                .map(NoticeAttachmentJpaEntity::toDomain);
    }

    @Override
    public void deleteById(Long noticeAttachmentId) {
        springDataNoticeAttachmentRepository.deleteById(noticeAttachmentId);
    }

    @Override
    public long countByNoticeId(Long noticeId) {
        return springDataNoticeAttachmentRepository.countByNoticeId(noticeId);
    }
}

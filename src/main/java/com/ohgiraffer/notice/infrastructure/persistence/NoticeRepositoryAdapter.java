package com.ohgiraffer.notice.infrastructure.persistence;

import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.repository.NoticeRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class NoticeRepositoryAdapter implements NoticeRepository {

    private final SpringDataNoticeRepository springDataNoticeRepository;

    public NoticeRepositoryAdapter(
            SpringDataNoticeRepository springDataNoticeRepository
    ) {
        this.springDataNoticeRepository = springDataNoticeRepository;
    }

    @Override
    public Notice save(Notice notice) {
        NoticeJpaEntity saved = springDataNoticeRepository.save(
                NoticeJpaEntity.from(notice)
        );

        return saved.toDomain();
    }

    @Override
    public Optional<Notice> findById(Long noticeId) {
        return springDataNoticeRepository.findById(noticeId)
                .map(NoticeJpaEntity::toDomain);
    }
}

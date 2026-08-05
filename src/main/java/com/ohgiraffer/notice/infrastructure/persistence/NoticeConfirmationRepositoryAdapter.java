package com.ohgiraffer.notice.infrastructure.persistence;

import com.ohgiraffer.notice.domain.repository.NoticeConfirmationRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

@Repository
public class NoticeConfirmationRepositoryAdapter
        implements NoticeConfirmationRepository {

    private final SpringDataNoticeConfirmationRepository
            springDataNoticeConfirmationRepository;

    public NoticeConfirmationRepositoryAdapter(
            SpringDataNoticeConfirmationRepository springDataNoticeConfirmationRepository
    ) {
        this.springDataNoticeConfirmationRepository =
                springDataNoticeConfirmationRepository;
    }

    @Override
    public void confirm(Long noticeId, Long userId) {
        if (existsBy(noticeId, userId)) {
            return;
        }

        springDataNoticeConfirmationRepository.save(
                NoticeConfirmationJpaEntity.of(noticeId, userId, Instant.now())
        );
    }

    @Override
    public boolean existsBy(Long noticeId, Long userId) {
        return springDataNoticeConfirmationRepository.existsById(
                new NoticeConfirmationId(noticeId, userId)
        );
    }

    @Override
    public long countBy(Long noticeId) {
        return springDataNoticeConfirmationRepository.countByNoticeId(noticeId);
    }

    @Override
    public Set<Long> findConfirmedNoticeIds(
            Long userId,
            Collection<Long> noticeIds
    ) {
        if (noticeIds.isEmpty()) {
            return Set.of();
        }

        return Set.copyOf(
                springDataNoticeConfirmationRepository
                        .findConfirmedNoticeIds(userId, noticeIds)
        );
    }
}

package com.ohgiraffer.notice.infrastructure.persistence;

import com.ohgiraffer.notice.domain.repository.NoticeConfirmationRepository;
import org.springframework.stereotype.Repository;

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
        /*
         * 이미 확인했는지 먼저 묻지 않는다. 묻고 저장하는 사이에 같은 사용자의
         * 두 번째 요청이 끼어들면 둘 다 저장을 시도해 복합 PK 중복으로 터진다.
         * 한 문장으로 넣고 중복은 DB가 무시하게 둔다.
         */
        springDataNoticeConfirmationRepository
                .insertIfAbsent(noticeId, userId);
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

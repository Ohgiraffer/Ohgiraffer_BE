package com.ohgiraffer.notice.infrastructure.persistence;

import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.model.ViewerRole;
import com.ohgiraffer.notice.domain.repository.NoticeRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Override
    public List<Notice> findAllVisible(ViewerRole viewer, Long categoryId) {
        return springDataNoticeRepository
                .findAllVisible(categoryId, traineeVisibilityFilter(viewer))
                .stream()
                .map(NoticeJpaEntity::toDomain)
                .toList();
    }

    /**
     * 훈련생에게는 공개 공지만, 운영진에게는 제한 없이 보여준다.
     * null 은 "공개 여부를 가리지 않는다"는 뜻이다.
     */
    private Boolean traineeVisibilityFilter(ViewerRole viewer) {
        return viewer == ViewerRole.TRAINEE ? Boolean.TRUE : null;
    }
}

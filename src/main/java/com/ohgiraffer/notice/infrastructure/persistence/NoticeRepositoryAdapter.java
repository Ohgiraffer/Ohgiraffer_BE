package com.ohgiraffer.notice.infrastructure.persistence;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
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
    public Notice update(Notice notice) {
        NoticeJpaEntity entity = springDataNoticeRepository
                .findById(notice.getId())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.NOTICE_NOT_FOUND));

        entity.applyUpdate(notice);

        /*
         * 변경 감지는 트랜잭션이 끝날 때 반영되는데, 응답에 수정 시각을 담아야 하므로
         * 여기서 flush 해 감사 기능이 updated_at 을 채우게 한다.
         */
        springDataNoticeRepository.flush();

        return entity.toDomain();
    }

    @Override
    public void deleteById(Long noticeId) {
        springDataNoticeRepository.deleteById(noticeId);
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

    @Override
    public long countByCategoryId(Long categoryId) {
        return springDataNoticeRepository.countByCategoryId(categoryId);
    }

    /**
     * 훈련생에게는 공개 공지만, 운영진에게는 제한 없이 보여준다.
     * null 은 "공개 여부를 가리지 않는다"는 뜻이다.
     */
    private Boolean traineeVisibilityFilter(ViewerRole viewer) {
        return viewer == ViewerRole.TRAINEE ? Boolean.TRUE : null;
    }
}

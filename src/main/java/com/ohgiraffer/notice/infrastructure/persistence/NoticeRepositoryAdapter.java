package com.ohgiraffer.notice.infrastructure.persistence;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.model.ViewerRole;
import com.ohgiraffer.notice.domain.repository.NoticeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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
        try {
            /*
             * saveAndFlush 로 INSERT 를 지금 내보낸다.
             * save 만 하면 트랜잭션이 끝날 때 나가 이 catch 를 지나쳐 버린다.
             */
            NoticeJpaEntity saved = springDataNoticeRepository.saveAndFlush(
                    NoticeJpaEntity.from(notice)
            );

            return saved.toDomain();
        } catch (DataIntegrityViolationException e) {
            /*
             * 서비스가 카테고리 존재를 미리 확인하지만, 확인과 저장 사이에 그 카테고리가
             * 삭제되면 외래키가 막는다. 이때도 없는 카테고리와 같은 404 로 알린다.
             *
             * notice 에는 작성자 외래키도 있어 제약 이름으로 가려낸다.
             * 작성자 쪽 위반은 인증을 통과한 사용자가 users 에 없다는 뜻이라 업무 오류가 아니다.
             */
            throw ConstraintViolations.translate(
                    e,
                    ConstraintViolations.NOTICE_TO_CATEGORY,
                    ErrorCode.NOTICE_CATEGORY_NOT_FOUND,
                    null
            );
        }
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
    public List<Notice> findDashboardSummary(
            ViewerRole viewer,
            Long userId,
            Instant since
    ) {
        return springDataNoticeRepository
                .findDashboardSummary(
                        traineeVisibilityFilter(viewer),
                        userId,
                        since
                )
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

package com.ohgiraffer.submissionbox.infrastructure.persistence;

import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;
import com.ohgiraffer.submissionbox.domain.repository.SubmissionBoxRepository;
import org.springframework.stereotype.Repository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.util.List;
import java.util.Optional;

@Repository
public class SubmissionBoxRepositoryAdapter implements SubmissionBoxRepository {

    private final SpringDataSubmissionBoxRepository repository;

    public SubmissionBoxRepositoryAdapter(
            SpringDataSubmissionBoxRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public SubmissionBox save(
            SubmissionBox submissionBox
    ) {
        SubmissionBoxJpaEntity entity =
                SubmissionBoxJpaEntity.from(submissionBox);

        SubmissionBoxJpaEntity savedEntity =
                repository.saveAndFlush(entity);

        return savedEntity.toDomain();
    }

    @Override
    public SubmissionBox update(
            SubmissionBox submissionBox
    ) {
        SubmissionBoxJpaEntity entity =
                repository.findWithItemsById(
                        submissionBox.getId()
                ).orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.SUBMISSION_BOX_NOT_FOUND
                        )
                );

        /*
         * 현재 항목 순서를 DB에서 사용하지 않는 임시 범위로 이동합니다.
         *
         * 예를 들어 기존 순서가 1, 2이고 요청 순서가 2, 1이면
         * 바로 UPDATE할 경우 UNIQUE(submission_box_id, sort_order)
         * 제약과 충돌할 수 있습니다.
         */
        entity.moveItemSortOrdersToTemporaryRange();
        repository.flush();

        /*
         * 임시 순서가 DB에 반영된 다음 최종 요청 내용을 적용합니다.
         * 신규 항목 추가와 기존 항목 삭제도 기존 sortOrder와
         * 충돌하지 않게 됩니다.
         */
        entity.updateFrom(submissionBox);

        SubmissionBoxJpaEntity savedEntity =
                repository.saveAndFlush(entity);

        return savedEntity.toDomain();
    }

    @Override
    public List<SubmissionBox> findAll() {
        return repository.findAllByOrderByDueAtDesc()
                .stream()
                .map(SubmissionBoxJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<SubmissionBox> findById(
            Long submissionBoxId
    ) {
        return repository.findWithItemsById(submissionBoxId)
                .map(SubmissionBoxJpaEntity::toDomain);
    }

    @Override
    public Optional<SubmissionBox> findByIdForUpdate(
            Long submissionBoxId
    ) {
        return repository
                .findByIdForUpdate(submissionBoxId)
                .map(entity -> {
                    /*
                     * 제출함 본체에 비관적 잠금을 획득한 다음
                     * 같은 트랜잭션에서 제출 항목을 초기화합니다.
                     *
                     * 컬렉션 fetch와 PESSIMISTIC_WRITE를 한 SQL에서
                     * 함께 사용할 때 발생할 수 있는 DB 잠금 오류를 피합니다.
                     */
                    entity.getItems().size();

                    return entity.toDomain();
                });
    }

    @Override
    public boolean existsById(
            Long submissionBoxId
    ) {
        return repository.existsById(submissionBoxId);
    }

    @Override
    public boolean hasSubmissions(
            Long submissionBoxId
    ) {
        return repository
                .existsSubmissionBySubmissionBoxId(
                        submissionBoxId
                );
    }

    @Override
    public void deleteById(
            Long submissionBoxId
    ) {
        repository.deleteById(submissionBoxId);
        repository.flush();
    }

    @Override
    public List<SubmissionBox> findAllByBootcampId(
            Long bootcampId
    ) {
        return repository
                .findAllByBootcampIdOrderByDueAtDesc(
                        bootcampId
                )
                .stream()
                .map(SubmissionBoxJpaEntity::toDomain)
                .toList();
    }
}
package com.ohgiraffer.submissionbox.infrastructure.persistence;

import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;
import com.ohgiraffer.submissionbox.domain.repository.SubmissionBoxRepository;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataIntegrityViolationException;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import org.hibernate.exception.ConstraintViolationException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class SubmissionBoxRepositoryAdapter implements SubmissionBoxRepository {


    /*
     * submission이 submission_box를 참조하는 외래 키입니다.
     * 이 제약 위반일 때만 "제출물이 존재하여 삭제 불가"로 변환합니다.
     */
    private static final String
            SUBMISSION_BOX_TO_SUBMISSION_FOREIGN_KEY =
            "FK_SUBMISSION_BOX_TO_SUBMISSION";

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
    public boolean hasSubmissions(Long submissionBoxId) {
        Long count =
                repository.countSubmissionsBySubmissionBoxId(
                        submissionBoxId
                );

        return count != null && count > 0L;
    }

    @Override
    public void deleteById(
            Long submissionBoxId
    ) {
        try {
            repository.deleteById(submissionBoxId);

            /*
             * deleteById() 호출만으로는 DELETE SQL이 즉시 실행되지
             * 않을 수 있습니다.
             *
             * flush()를 호출해 현재 메서드 안에서 DELETE SQL을 실행하고,
             * 외래 키 위반도 이 catch 블록에서 처리합니다.
             */
            repository.flush();
        } catch (DataIntegrityViolationException exception) {

            /*
             * submission이 해당 제출함을 참조하고 있는 경우에만
             * "제출물이 존재하여 삭제 불가" 업무 오류로 변환합니다.
             */
            if (isSubmissionForeignKeyViolation(exception)) {
                throw new BusinessException(
                        ErrorCode.SUBMISSION_BOX_HAS_SUBMISSIONS,
                        "제출 데이터가 연결된 제출함은 삭제할 수 없습니다.",
                        exception
                );
            }

            /*
             * 다른 UNIQUE, CHECK, FK 제약 위반은 제출물 존재 오류가
             * 아니므로 원래 예외를 다시 발생시킵니다.
             *
             * 그래야 실제 DB 오류가 SUBMISSION_002로 잘못 숨겨지지
             * 않습니다.
             */
            throw exception;
        }
    }

    /**
     * submission이 submission_box를 참조하는 외래 키 위반인지 확인합니다.
     *
     * Hibernate 또는 JDBC 드라이버가 예외를 여러 번 감싸서 전달할 수
     * 있으므로 전체 원인 예외 체인을 순회합니다.
     */
    private boolean isSubmissionForeignKeyViolation(
            DataIntegrityViolationException exception
    ) {
        Throwable cause = exception;

        while (cause != null) {

            /*
             * Hibernate가 제약조건 이름을 구조화된 값으로 제공하는 경우
             */
            if (cause
                    instanceof ConstraintViolationException
                    constraintViolationException) {

                String constraintName =
                        constraintViolationException
                                .getConstraintName();

                if (constraintName != null
                        && SUBMISSION_BOX_TO_SUBMISSION_FOREIGN_KEY
                        .equalsIgnoreCase(constraintName)) {
                    return true;
                }
            }

            /*
             * MySQL JDBC 예외 메시지 안에 제약조건 이름이 들어오는 경우
             */
            if (cause instanceof SQLException sqlException) {
                String message = sqlException.getMessage();

                if (containsConstraintName(message)) {
                    return true;
                }
            }

            /*
             * 예외 종류가 달라도 메시지에 제약조건 이름이 들어 있을 수
             * 있으므로 한 번 더 확인합니다.
             */
            if (containsConstraintName(cause.getMessage())) {
                return true;
            }

            Throwable nextCause = cause.getCause();

            /*
             * 비정상적으로 자기 자신을 cause로 가진 예외가 있을 때
             * 무한 반복을 방지합니다.
             */
            if (nextCause == cause) {
                break;
            }

            cause = nextCause;
        }

        return false;
    }

    private boolean containsConstraintName(
            String message
    ) {
        if (message == null) {
            return false;
        }

        return message
                .toLowerCase()
                .contains(
                        SUBMISSION_BOX_TO_SUBMISSION_FOREIGN_KEY
                                .toLowerCase()
                );
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
package com.ohgiraffer.submission.infrastructure.persistence;

import com.ohgiraffer.submission.domain.model.Submission;
import com.ohgiraffer.submission.domain.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import com.ohgiraffer.submission.domain.model.SubmissionListEntry;
import java.util.Collection;

import java.util.Optional;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SubmissionRepositoryAdapter
        implements SubmissionRepository {

    private final SpringDataSubmissionRepository repository;

    @Override
    public Submission save(
            Submission submission
    ) {
        try {
            SubmissionJpaEntity entity;

            if (submission.getId() == null) {
                /*
                 * 최초 제출은 새로운 JPA 엔티티를 생성합니다.
                 */
                entity =
                        SubmissionJpaEntity.from(
                                submission
                        );
            } else {
                /*
                 * 재제출은 기존 관리 엔티티를 조회한 뒤
                 * 값과 자식 컬렉션을 직접 변경합니다.
                 */
                entity =
                        repository.findDetailById(
                                        submission.getId()
                                )
                                .orElseThrow(() ->
                                        new BusinessException(
                                                ErrorCode.SUBMISSION_NOT_FOUND
                                        )
                                );

                entity.updateFrom(submission);
            }

            SubmissionJpaEntity savedEntity =
                    repository.saveAndFlush(entity);

            return savedEntity.toDomain();
        } catch (DataIntegrityViolationException exception) {
            if (isDuplicateSubmissionViolation(exception)) {
                throw new BusinessException(
                        ErrorCode.SUBMISSION_ALREADY_EXISTS
                );
            }

            throw exception;
        }
    }

    private boolean isDuplicateSubmissionViolation(
            DataIntegrityViolationException exception
    ) {
        Throwable current = exception;

        while (current != null) {
            String message = current.getMessage();

            if (message != null
                    && (message.contains(
                    "UQ_SUBMISSION_BOX_OWNER_USER"
            )
                    || message.contains(
                    "UQ_SUBMISSION_BOX_TEAM"
            ))) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }

    @Override
    public boolean existsBySubmissionBoxIdAndOwnerUserId(
            Long submissionBoxId,
            Long ownerUserId
    ) {
        return repository
                .existsBySubmissionBoxIdAndOwnerUserId(
                        submissionBoxId,
                        ownerUserId
                );
    }

    @Override
    public boolean existsBySubmissionBoxIdAndTeamId(
            Long submissionBoxId,
            Long teamId
    ) {
        return repository
                .existsBySubmissionBoxIdAndTeamId(
                        submissionBoxId,
                        teamId
                );
    }

    @Override
    public Optional<Submission>
    findBySubmissionBoxIdAndOwnerUserId(
            Long submissionBoxId,
            Long ownerUserId
    ) {
        return repository
                .findBySubmissionBoxIdAndOwnerUserId(
                        submissionBoxId,
                        ownerUserId
                )
                .map(SubmissionJpaEntity::toDomain);
    }

    @Override
    public Optional<Submission>
    findBySubmissionBoxIdAndTeamId(
            Long submissionBoxId,
            Long teamId
    ) {
        return repository
                .findBySubmissionBoxIdAndTeamId(
                        submissionBoxId,
                        teamId
                )
                .map(SubmissionJpaEntity::toDomain);
    }

    @Override
    public List<Submission> findAllBySubmissionBoxId(
            Long submissionBoxId
    ) {
        return repository
                .findAllBySubmissionBoxIdOrderBySubmittedAtAsc(
                        submissionBoxId
                )
                .stream()
                .map(SubmissionJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<SubmissionListEntry>
    findListEntriesBySubmissionBoxIds(
            Collection<Long> submissionBoxIds
    ) {
        if (submissionBoxIds == null
                || submissionBoxIds.isEmpty()) {
            return List.of();
        }

        return repository
                .findListEntriesBySubmissionBoxIds(
                        submissionBoxIds
                );
    }

    @Override
    public Optional<Submission> findById(
            Long submissionId
    ) {
        return repository
                .findDetailById(submissionId)
                .map(SubmissionJpaEntity::toDomain);
    }

}
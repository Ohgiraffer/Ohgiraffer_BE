package com.ohgiraffer.evaluation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataEvaluationSheetLinkRepository
        extends JpaRepository<EvaluationSheetLinkJpaEntity, Long> {

    /**
     * 도메인 컬럼에 유니크 제약이 있어 결과는 없거나 하나다.
     */
    Optional<EvaluationSheetLinkJpaEntity> findByDomain(String domain);
}

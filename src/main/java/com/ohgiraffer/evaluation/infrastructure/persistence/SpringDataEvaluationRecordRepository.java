package com.ohgiraffer.evaluation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataEvaluationRecordRepository
        extends JpaRepository<EvaluationRecordJpaEntity, Long> {

    List<EvaluationRecordJpaEntity> findAllBySheetLinkId(Long sheetLinkId);
}

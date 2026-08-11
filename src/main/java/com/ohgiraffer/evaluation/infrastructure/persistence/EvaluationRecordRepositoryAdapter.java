package com.ohgiraffer.evaluation.infrastructure.persistence;

import com.ohgiraffer.evaluation.domain.model.EvaluationRecord;
import com.ohgiraffer.evaluation.domain.repository.EvaluationRecordRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EvaluationRecordRepositoryAdapter
        implements EvaluationRecordRepository {

    private final SpringDataEvaluationRecordRepository
            springDataEvaluationRecordRepository;

    public EvaluationRecordRepositoryAdapter(
            SpringDataEvaluationRecordRepository springDataEvaluationRecordRepository
    ) {
        this.springDataEvaluationRecordRepository =
                springDataEvaluationRecordRepository;
    }

    @Override
    public List<EvaluationRecord> findAllBySheetLinkId(Long sheetLinkId) {
        return springDataEvaluationRecordRepository
                .findAllBySheetLinkId(sheetLinkId)
                .stream()
                .map(EvaluationRecordJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<EvaluationRecord> saveAll(List<EvaluationRecord> records) {
        if (records.isEmpty()) {
            return List.of();
        }

        return springDataEvaluationRecordRepository
                .saveAll(records.stream()
                        .map(EvaluationRecordJpaEntity::from)
                        .toList())
                .stream()
                .map(EvaluationRecordJpaEntity::toDomain)
                .toList();
    }
}

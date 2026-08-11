package com.ohgiraffer.evaluation.infrastructure.persistence;

import com.ohgiraffer.evaluation.domain.model.EvaluationSheetLink;
import com.ohgiraffer.evaluation.domain.repository.EvaluationSheetLinkRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class EvaluationSheetLinkRepositoryAdapter
        implements EvaluationSheetLinkRepository {

    private final SpringDataEvaluationSheetLinkRepository
            springDataEvaluationSheetLinkRepository;

    public EvaluationSheetLinkRepositoryAdapter(
            SpringDataEvaluationSheetLinkRepository springDataEvaluationSheetLinkRepository
    ) {
        this.springDataEvaluationSheetLinkRepository =
                springDataEvaluationSheetLinkRepository;
    }

    @Override
    public EvaluationSheetLink save(EvaluationSheetLink sheetLink) {
        return springDataEvaluationSheetLinkRepository
                .save(EvaluationSheetLinkJpaEntity.from(sheetLink))
                .toDomain();
    }

    @Override
    public Optional<EvaluationSheetLink> find() {
        return springDataEvaluationSheetLinkRepository
                .findByDomain(EvaluationSheetLink.DOMAIN)
                .map(EvaluationSheetLinkJpaEntity::toDomain);
    }
}

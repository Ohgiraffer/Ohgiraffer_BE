package com.ohgiraffer.evaluation.infrastructure.persistence;

import com.ohgiraffer.evaluation.domain.model.SheetSyncLog;
import com.ohgiraffer.evaluation.domain.repository.SheetSyncLogRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SheetSyncLogRepositoryAdapter implements SheetSyncLogRepository {

    private final SpringDataSheetSyncLogRepository
            springDataSheetSyncLogRepository;

    public SheetSyncLogRepositoryAdapter(
            SpringDataSheetSyncLogRepository springDataSheetSyncLogRepository
    ) {
        this.springDataSheetSyncLogRepository = springDataSheetSyncLogRepository;
    }

    @Override
    public SheetSyncLog save(SheetSyncLog syncLog) {
        return springDataSheetSyncLogRepository
                .save(SheetSyncLogJpaEntity.from(syncLog))
                .toDomain();
    }

    @Override
    public List<SheetSyncLog> findAllBySheetLinkId(Long sheetLinkId) {
        return springDataSheetSyncLogRepository
                .findAllBySheetLinkIdOrderByIdDesc(sheetLinkId)
                .stream()
                .map(SheetSyncLogJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<SheetSyncLog> findById(Long syncLogId) {
        return springDataSheetSyncLogRepository.findById(syncLogId)
                .map(SheetSyncLogJpaEntity::toDomain);
    }
}

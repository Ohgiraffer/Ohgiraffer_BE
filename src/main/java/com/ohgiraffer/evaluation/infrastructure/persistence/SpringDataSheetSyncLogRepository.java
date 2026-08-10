package com.ohgiraffer.evaluation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataSheetSyncLogRepository
        extends JpaRepository<SheetSyncLogJpaEntity, Long> {

    List<SheetSyncLogJpaEntity> findAllBySheetLinkIdOrderByIdDesc(Long sheetLinkId);
}

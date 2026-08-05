package com.ohgiraffer.approval.domain.repository;

import com.ohgiraffer.approval.domain.model.budget.ExternalSheetLink;

import java.util.Optional;

public interface ExternalSheetLinkRepository {

    ExternalSheetLink save(
            ExternalSheetLink externalSheetLink
    );

    Optional<ExternalSheetLink> findByDomain(
            String domain
    );
}
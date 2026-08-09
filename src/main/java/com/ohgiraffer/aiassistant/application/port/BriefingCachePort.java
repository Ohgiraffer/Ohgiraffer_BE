package com.ohgiraffer.aiassistant.application.port;

import com.ohgiraffer.aiassistant.domain.model.BriefingSummary;

import java.util.Optional;

public interface BriefingCachePort {

    Optional<BriefingSummary> find(Long userId);

    void save(BriefingSummary summary);

}

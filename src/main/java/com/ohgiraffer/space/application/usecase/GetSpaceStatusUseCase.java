package com.ohgiraffer.space.application.usecase;

import java.util.List;

public interface GetSpaceStatusUseCase {

    List<SpaceStatusResult> getSpaceStatuses(
            Long requesterId
    );
}
package com.ohgiraffer.space.application.service;

import com.ohgiraffer.space.application.port.SpaceStatusData;
import com.ohgiraffer.space.application.port.SpaceStatusQueryPort;
import com.ohgiraffer.space.application.usecase.GetSpaceStatusUseCase;
import com.ohgiraffer.space.application.usecase.SpaceStatusResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuerySpaceStatusService
        implements GetSpaceStatusUseCase {

    private final SpaceStatusQueryPort spaceStatusQueryPort;

    @Override
    public List<SpaceStatusResult> getSpaceStatuses(
            Long requesterId
    ) {
        List<SpaceStatusData> statuses =
                spaceStatusQueryPort.findAllSpaceStatuses();

        return statuses.stream()
                .map(status ->
                        SpaceStatusResult.from(
                                status,
                                requesterId
                        )
                )
                .toList();
    }
}
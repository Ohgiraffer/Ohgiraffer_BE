package com.ohgiraffer.space.application.service;

import com.ohgiraffer.global.s3.S3UrlResolver;
import com.ohgiraffer.space.application.port.SpaceStatusData;
import com.ohgiraffer.space.application.port.SpaceStatusQueryPort;
import com.ohgiraffer.space.application.usecase.GetSpaceStatusUseCase;
import com.ohgiraffer.space.application.usecase.SpaceStatusResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuerySpaceStatusService
        implements GetSpaceStatusUseCase {

    private static final ZoneId SERVICE_ZONE =
            ZoneId.of("Asia/Seoul");

    private final SpaceStatusQueryPort
            spaceStatusQueryPort;

    private final S3UrlResolver
            s3UrlResolver;

    @Override
    public List<SpaceStatusResult> getSpaceStatuses(
            Long requesterId
    ) {
        LocalDate today =
                LocalDate.now(SERVICE_ZONE);

        List<SpaceStatusData> statuses =
                spaceStatusQueryPort
                        .findAllSpaceStatuses(today);

        return statuses.stream()
                .map(status ->
                        SpaceStatusResult.from(
                                status,
                                requesterId,
                                s3UrlResolver::resolve
                        )
                )
                .toList();
    }
}
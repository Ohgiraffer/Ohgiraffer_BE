package com.ohgiraffer.space.application.port;

import java.time.LocalDate;
import java.util.List;

public interface SpaceStatusQueryPort {

    List<SpaceStatusData> findAllSpaceStatuses(
            LocalDate locationDate
    );
}
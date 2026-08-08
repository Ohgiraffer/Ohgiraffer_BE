package com.ohgiraffer.space.application.port;

import java.util.List;

public interface SpaceStatusQueryPort {

    List<SpaceStatusData> findAllSpaceStatuses();
}
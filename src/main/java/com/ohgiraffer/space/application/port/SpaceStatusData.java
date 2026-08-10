package com.ohgiraffer.space.application.port;

import java.util.List;

public record SpaceStatusData(
        Long spaceId,
        String spaceName,
        int capacity,
        List<SpaceOccupantData> occupants
) {

    public SpaceStatusData {
        occupants = List.copyOf(occupants);
    }
}
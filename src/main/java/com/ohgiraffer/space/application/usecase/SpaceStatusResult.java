package com.ohgiraffer.space.application.usecase;

import com.ohgiraffer.space.application.port.SpaceStatusData;

import java.util.List;

public record SpaceStatusResult(
        Long spaceId,
        String spaceName,
        int capacity,
        int currentCount,
        int availableCount,
        List<SpaceOccupantResult> occupants
) {

    public SpaceStatusResult {
        occupants = List.copyOf(occupants);
    }

    public static SpaceStatusResult from(
            SpaceStatusData data,
            Long requesterId
    ) {
        List<SpaceOccupantResult> occupants =
                data.occupants()
                        .stream()
                        .map(occupant ->
                                SpaceOccupantResult.from(
                                        occupant,
                                        requesterId
                                )
                        )
                        .toList();

        int currentCount = occupants.size();
        int availableCount =
                Math.max(data.capacity() - currentCount, 0);

        return new SpaceStatusResult(
                data.spaceId(),
                data.spaceName(),
                data.capacity(),
                currentCount,
                availableCount,
                occupants
        );
    }
}
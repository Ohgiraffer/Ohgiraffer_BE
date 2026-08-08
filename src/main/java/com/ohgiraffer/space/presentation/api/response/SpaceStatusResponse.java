package com.ohgiraffer.space.presentation.api.response;

import com.ohgiraffer.space.application.usecase.SpaceStatusResult;

import java.util.List;

public record SpaceStatusResponse(
        Long spaceId,
        String spaceName,
        int capacity,
        int currentCount,
        int availableCount,
        List<SpaceOccupantResponse> occupants
) {

    public SpaceStatusResponse {
        occupants = List.copyOf(occupants);
    }

    public static SpaceStatusResponse from(
            SpaceStatusResult result
    ) {
        List<SpaceOccupantResponse> occupants =
                result.occupants()
                        .stream()
                        .map(SpaceOccupantResponse::from)
                        .toList();

        return new SpaceStatusResponse(
                result.spaceId(),
                result.spaceName(),
                result.capacity(),
                result.currentCount(),
                result.availableCount(),
                occupants
        );
    }
}
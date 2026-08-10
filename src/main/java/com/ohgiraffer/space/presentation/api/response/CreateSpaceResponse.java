package com.ohgiraffer.space.presentation.api.response;

import com.ohgiraffer.space.application.usecase.CreateSpaceResult;

import java.util.List;

public record CreateSpaceResponse(
        Long spaceId,
        String spaceName,
        int capacity,
        int currentCount,
        int availableCount,
        List<SpaceOccupantResponse> occupants
) {

    public CreateSpaceResponse {
        occupants = List.copyOf(occupants);
    }

    public static CreateSpaceResponse from(
            CreateSpaceResult result
    ) {
        return new CreateSpaceResponse(
                result.spaceId(),
                result.spaceName(),
                result.capacity(),
                result.currentCount(),
                result.availableCount(),
                List.of()
        );
    }
}
package com.ohgiraffer.space.application.usecase;

import com.ohgiraffer.space.domain.model.Space;

public record CreateSpaceResult(
        Long spaceId,
        String spaceName,
        int capacity,
        int currentCount,
        int availableCount
) {

    public static CreateSpaceResult from(
            Space space
    ) {
        return new CreateSpaceResult(
                space.getId(),
                space.getName(),
                space.getCapacity(),
                0,
                space.getCapacity()
        );
    }
}
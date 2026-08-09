package com.ohgiraffer.space.presentation.api.response;

import com.ohgiraffer.space.application.usecase.SpaceStatusResult;

import java.util.List;

public record SpaceStatusListResponse(
        List<SpaceStatusResponse> spaces
) {

    public SpaceStatusListResponse {
        spaces = List.copyOf(spaces);
    }

    public static SpaceStatusListResponse from(
            List<SpaceStatusResult> results
    ) {
        List<SpaceStatusResponse> spaces =
                results.stream()
                        .map(SpaceStatusResponse::from)
                        .toList();

        return new SpaceStatusListResponse(spaces);
    }
}
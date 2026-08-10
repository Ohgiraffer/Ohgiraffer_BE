package com.ohgiraffer.space.presentation.api.request;

import com.ohgiraffer.space.application.command.UpdateMyLocationCommand;

public record UpdateMyLocationRequest(
        Long spaceId
) {

    public UpdateMyLocationCommand toCommand() {
        return new UpdateMyLocationCommand(
                spaceId
        );
    }
}
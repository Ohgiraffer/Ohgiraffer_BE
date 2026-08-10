package com.ohgiraffer.space.application.command;

public record CreateSpaceCommand(
        String name,
        int capacity
) {
}
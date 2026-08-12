package com.ohgiraffer.team.application.event;

import java.util.List;

public record TeamConfigurationSavedEvent(
        List<Long> sendbirdOutboxIds,
        List<Long> notionOutboxIds
) {

    public TeamConfigurationSavedEvent {
        sendbirdOutboxIds =
                sendbirdOutboxIds == null
                        ? List.of()
                        : List.copyOf(
                        sendbirdOutboxIds
                );

        notionOutboxIds =
                notionOutboxIds == null
                        ? List.of()
                        : List.copyOf(
                        notionOutboxIds
                );
    }
}
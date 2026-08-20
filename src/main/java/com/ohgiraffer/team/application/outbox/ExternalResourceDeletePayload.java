package com.ohgiraffer.team.application.outbox;

public record ExternalResourceDeletePayload(
        Long teamPeriodId,
        Long teamId,
        String sendbirdChannelUrl,
        String notionPageId
) {
}
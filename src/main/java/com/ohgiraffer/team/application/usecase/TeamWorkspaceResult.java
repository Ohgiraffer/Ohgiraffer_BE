package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.team.domain.model.Team;

public record TeamWorkspaceResult(
        Long teamId,
        String notionPageId,
        String workspaceUrl
) {

    private static final String NOTION_PAGE_URL_PREFIX =
            "https://www.notion.so/";

    public static TeamWorkspaceResult from(
            Team team
    ) {
        String notionPageId =
                team.getNotionPageId();

        return new TeamWorkspaceResult(
                team.getId(),
                notionPageId,
                createWorkspaceUrl(
                        notionPageId
                )
        );
    }

    private static String createWorkspaceUrl(
            String notionPageId
    ) {
        if (notionPageId == null
                || notionPageId.isBlank()) {
            return null;
        }

        return NOTION_PAGE_URL_PREFIX
                + notionPageId.replace(
                "-",
                ""
        );
    }
}
package com.ohgiraffer.team.infrastructure.notion;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.team.application.port.TeamWorkspacePort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class NotionTeamWorkspaceAdapter implements TeamWorkspacePort {

    private static final String TEAM_NAME_PROPERTY = "팀명";
    private static final String TEAM_ID_PROPERTY = "TeamId";
    private static final String TEAM_MEMBERS_PROPERTY = "팀원";
    private static final String TIMEZONE = "Asia/Seoul";
    private static final int MAX_TITLE_LENGTH = 1900;
    private static final int MAX_MULTI_SELECT_NAME_LENGTH = 100;

    private final RestClient notionRestClient;
    private final NotionProperties properties;

    public NotionTeamWorkspaceAdapter(
            @Qualifier("notionRestClient") RestClient notionRestClient,
            NotionProperties properties
    ) {
        this.notionRestClient = notionRestClient;
        this.properties = properties;
    }

    @Override
    public Optional<String> findPageIdByTeamId(Long teamId) {
        try {
            NotionQueryResponse response =
                    notionRestClient.post()
                            .uri("/data_sources/{dataSourceId}/query", properties.dataSourceId())
                            .body(Map.of(
                                    "filter", Map.of(
                                            "property", TEAM_ID_PROPERTY,
                                            "number", Map.of("equals", teamId)
                                    ),
                                    "page_size", 2
                            ))
                            .retrieve()
                            .body(NotionQueryResponse.class);

            if (response == null
                    || response.results() == null
                    || response.results().isEmpty()) {
                return Optional.empty();
            }

            if (response.results().size() > 1) {
                throw new BusinessException(
                        ErrorCode.TEAM_NOTION_API_ERROR,
                        "동일한 TeamId를 가진 Notion 페이지가 2개 이상 존재합니다."
                );
            }

            return Optional.ofNullable(response.results().get(0).id());
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new BusinessException(
                    ErrorCode.TEAM_NOTION_API_ERROR,
                    "Notion 페이지 조회에 실패했습니다."
            );
        }
    }

    @Override
    public String createTeamPage(
            Long teamId,
            String teamName,
            List<String> memberNames
    ) {
        try {
            NotionPageResponse response =
                    notionRestClient.post()
                            .uri("/pages")
                            .body(Map.of(
                                    "parent", Map.of(
                                            "type", "data_source_id",
                                            "data_source_id", properties.dataSourceId()
                                    ),
                                    "properties", createProperties(
                                            teamId,
                                            teamName,
                                            memberNames
                                    ),
                                    "template", Map.of(
                                            "type", "template_id",
                                            "template_id", properties.templateId(),
                                            "timezone", TIMEZONE
                                    )
                            ))
                            .retrieve()
                            .body(NotionPageResponse.class);

            if (response == null || response.id() == null || response.id().isBlank()) {
                throw new BusinessException(
                        ErrorCode.TEAM_NOTION_API_ERROR,
                        "Notion 페이지 ID를 응답받지 못했습니다."
                );
            }

            return response.id();
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new BusinessException(
                    ErrorCode.TEAM_NOTION_API_ERROR,
                    "Notion 페이지 생성에 실패했습니다."
            );
        }
    }

    @Override
    public void updateTeamPage(
            String notionPageId,
            Long teamId,
            String teamName,
            List<String> memberNames
    ) {
        try {
            notionRestClient.patch()
                    .uri("/pages/{pageId}", notionPageId)
                    .body(Map.of(
                            "properties", createProperties(
                                    teamId,
                                    teamName,
                                    memberNames
                            )
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new BusinessException(
                    ErrorCode.TEAM_NOTION_API_ERROR,
                    "Notion 페이지 수정에 실패했습니다."
            );
        }
    }

    @Override
    public void archiveTeamPage(
            String notionPageId
    ) {
        try {
            notionRestClient.patch()
                    .uri("/pages/{pageId}", notionPageId)
                    .body(Map.of(
                            "archived", true
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new BusinessException(
                    ErrorCode.TEAM_NOTION_API_ERROR,
                    "Notion 페이지 삭제 처리에 실패했습니다."
            );
        }
    }

    private Map<String, Object> createProperties(
            Long teamId,
            String teamName,
            List<String> memberNames
    ) {
        return Map.of(
                TEAM_NAME_PROPERTY, Map.of(
                        "title", List.of(
                                Map.of(
                                        "text", Map.of(
                                                "content", toNotionTitle(teamName)
                                        )
                                )
                        )
                ),
                TEAM_ID_PROPERTY, Map.of(
                        "number", teamId
                ),
                TEAM_MEMBERS_PROPERTY, Map.of(
                        "multi_select", createMemberMultiSelectOptions(memberNames)
                )
        );
    }

    private List<Map<String, Object>> createMemberMultiSelectOptions(
            List<String> memberNames
    ) {
        if (memberNames == null || memberNames.isEmpty()) {
            return List.of();
        }

        return memberNames.stream()
                .filter(this::hasText)
                .map(String::trim)
                .distinct()
                .map(memberName -> Map.<String, Object>of(
                        "name", toNotionMultiSelectName(memberName)
                ))
                .toList();
    }

    private String toNotionTitle(String value) {
        String trimmedValue =
                value == null ? "" : value.trim();

        if (trimmedValue.length() <= MAX_TITLE_LENGTH) {
            return trimmedValue;
        }

        return trimmedValue.substring(0, MAX_TITLE_LENGTH);
    }

    private String toNotionMultiSelectName(String value) {
        String trimmedValue =
                value == null ? "" : value.trim();

        if (trimmedValue.length() <= MAX_MULTI_SELECT_NAME_LENGTH) {
            return trimmedValue;
        }

        return trimmedValue.substring(0, MAX_MULTI_SELECT_NAME_LENGTH);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record NotionQueryResponse(
            List<NotionPageResponse> results
    ) {
    }

    private record NotionPageResponse(
            String id
    ) {
    }
}
package com.ohgiraffer.team.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffer.team.application.event.TeamChannelSyncTarget;
import com.ohgiraffer.team.application.outbox.ExternalResourceDeletePayload;
import com.ohgiraffer.team.application.outbox.NotionWorkspaceSyncPayload;
import com.ohgiraffer.team.application.outbox.SendbirdChannelSyncPayload;
import com.ohgiraffer.team.domain.model.TeamOutbox;
import com.ohgiraffer.team.domain.model.TeamOutboxStatus;
import com.ohgiraffer.team.domain.repository.TeamOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamOutboxProcessor {

    private static final int RETRY_BATCH_SIZE = 20;
    private static final Duration PROCESSING_TIMEOUT = Duration.ofMinutes(10);

    private final TeamOutboxRepository teamOutboxRepository;
    private final TeamOutboxService teamOutboxService;
    private final TeamSendbirdChannelSyncService teamSendbirdChannelSyncService;
    private final TeamNotionWorkspaceService teamNotionWorkspaceService;
    private final TeamSendbirdChannelDeleteService teamSendbirdChannelDeleteService;
    private final TeamNotionWorkspaceArchiveService teamNotionWorkspaceArchiveService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public void process(
            Long outboxId
    ) {
        String processingToken =
                null;

        try {
            Optional<TeamOutbox> processingOutbox =
                    teamOutboxService.markProcessing(
                            outboxId,
                            processingTimeoutAt()
                    );

            if (processingOutbox.isEmpty()) {
                return;
            }

            TeamOutbox outbox =
                    processingOutbox.get();

            processingToken =
                    outbox.getProcessingToken();

            processOutbox(
                    outbox
            );

            teamOutboxService.markSucceeded(
                    outboxId,
                    processingToken
            );
        } catch (Throwable exception) {
            teamOutboxService.markFailed(
                    outboxId,
                    processingToken,
                    exception
            );

            log.error(
                    "팀 아웃박스 처리에 실패했습니다. outboxId={}",
                    outboxId,
                    exception
            );
        }
    }

    @Transactional
    public void processRetryTargets() {
        LocalDateTime now =
                LocalDateTime.now(
                        clock
                );

        LocalDateTime processingTimeoutAt =
                now.minus(
                        PROCESSING_TIMEOUT
                );

        List<TeamOutbox> retryTargets =
                teamOutboxRepository.findRetryTargets(
                        List.of(
                                TeamOutboxStatus.PENDING,
                                TeamOutboxStatus.FAILED
                        ),
                        TeamOutboxStatus.PROCESSING,
                        now,
                        processingTimeoutAt,
                        RETRY_BATCH_SIZE
                );

        retryTargets.forEach(outbox ->
                process(
                        outbox.getId()
                )
        );
    }

    private LocalDateTime processingTimeoutAt() {
        return LocalDateTime.now(
                        clock
                )
                .minus(
                        PROCESSING_TIMEOUT
                );
    }

    private void processOutbox(
            TeamOutbox outbox
    ) {
        switch (outbox.getType()) {
            case SENDBIRD_CHANNEL_SYNC ->
                    processSendbirdChannelSync(
                            outbox.getPayload()
                    );
            case NOTION_WORKSPACE_SYNC ->
                    processNotionWorkspaceSync(
                            outbox.getPayload()
                    );
            case EXTERNAL_RESOURCE_DELETE ->
                    processExternalResourceDelete(
                            outbox.getPayload()
                    );
        }
    }

    private void processSendbirdChannelSync(
            String payload
    ) {
        SendbirdChannelSyncPayload sendbirdPayload =
                deserialize(
                        payload,
                        SendbirdChannelSyncPayload.class
                );

        teamSendbirdChannelSyncService.sync(
                new TeamChannelSyncTarget(
                        sendbirdPayload.teamId(),
                        sendbirdPayload.memberUserIds()
                ),
                sendbirdPayload.createChatChannel()
        );
    }

    private void processNotionWorkspaceSync(
            String payload
    ) {
        NotionWorkspaceSyncPayload notionPayload =
                deserialize(
                        payload,
                        NotionWorkspaceSyncPayload.class
                );

        teamNotionWorkspaceService.syncTeamWorkspace(
                notionPayload.teamId()
        );
    }

    private void processExternalResourceDelete(
            String payload
    ) {
        ExternalResourceDeletePayload deletePayload =
                deserialize(
                        payload,
                        ExternalResourceDeletePayload.class
                );

        if (deletePayload.sendbirdChannelUrl() != null
                && !deletePayload.sendbirdChannelUrl()
                .isBlank()) {
            teamSendbirdChannelDeleteService.delete(
                    deletePayload.sendbirdChannelUrl()
            );
        }

        if (deletePayload.notionPageId() != null
                && !deletePayload.notionPageId()
                .isBlank()) {
            teamNotionWorkspaceArchiveService.archive(
                    deletePayload.notionPageId()
            );
        }
    }

    private <T> T deserialize(
            String payload,
            Class<T> payloadType
    ) {
        try {
            return objectMapper.readValue(
                    payload,
                    payloadType
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "팀 아웃박스 payload 역직렬화에 실패했습니다.",
                    exception
            );
        }
    }
}
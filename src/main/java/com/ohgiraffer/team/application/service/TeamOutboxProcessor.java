package com.ohgiraffer.team.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
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

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamOutboxProcessor {

    private static final int RETRY_BATCH_SIZE = 20;
    private static final long PROCESSING_TIMEOUT_MINUTES = 10L;

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
        try {
            Optional<TeamOutbox> processingOutbox =
                    teamOutboxService.markProcessing(
                            outboxId,
                            processingTimeoutAt()
                    );

            if (processingOutbox.isEmpty()) {
                return;
            }

            processOutbox(
                    processingOutbox.get()
            );

            teamOutboxService.markSucceeded(
                    outboxId
            );
        } catch (Throwable exception) {
            teamOutboxService.markFailed(
                    outboxId,
                    exception
            );

            log.error(
                    "[TeamOutbox] 외부 동기화 작업 실패 | outboxId={}",
                    outboxId,
                    exception
            );
        }
    }

    public void processRetryTargets() {
        LocalDateTime now =
                now();

        LocalDateTime processingTimeoutAt =
                now.minusMinutes(
                        PROCESSING_TIMEOUT_MINUTES
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
        SendbirdChannelSyncPayload syncPayload =
                deserialize(
                        payload,
                        SendbirdChannelSyncPayload.class
                );

        teamSendbirdChannelSyncService.sync(
                new TeamChannelSyncTarget(
                        syncPayload.teamId(),
                        syncPayload.memberUserIds()
                ),
                syncPayload.createChatChannel()
        );
    }

    private void processNotionWorkspaceSync(
            String payload
    ) {
        NotionWorkspaceSyncPayload syncPayload =
                deserialize(
                        payload,
                        NotionWorkspaceSyncPayload.class
                );

        teamNotionWorkspaceService.syncTeamWorkspace(
                syncPayload.teamId()
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
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "Outbox payload 역직렬화에 실패했습니다."
            );
        }
    }

    private LocalDateTime processingTimeoutAt() {
        return now().minusMinutes(
                PROCESSING_TIMEOUT_MINUTES
        );
    }

    private LocalDateTime now() {
        return LocalDateTime.now(
                clock
        );
    }
}
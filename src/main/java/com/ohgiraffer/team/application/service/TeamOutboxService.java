package com.ohgiraffer.team.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.team.application.event.TeamChannelSyncTarget;
import com.ohgiraffer.team.application.event.TeamExternalResourceDeleteTarget;
import com.ohgiraffer.team.application.event.TeamWorkspaceSyncTarget;
import com.ohgiraffer.team.application.outbox.ExternalResourceDeletePayload;
import com.ohgiraffer.team.application.outbox.NotionWorkspaceSyncPayload;
import com.ohgiraffer.team.application.outbox.SendbirdChannelSyncPayload;
import com.ohgiraffer.team.domain.model.TeamOutbox;
import com.ohgiraffer.team.domain.model.TeamOutboxType;
import com.ohgiraffer.team.domain.repository.TeamOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TeamOutboxService {

    private final TeamOutboxRepository teamOutboxRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Transactional
    public TeamOutbox saveSendbirdChannelSync(
            TeamChannelSyncTarget target,
            boolean createChatChannel
    ) {
        SendbirdChannelSyncPayload payload =
                new SendbirdChannelSyncPayload(
                        target.teamId(),
                        target.memberUserIds(),
                        createChatChannel
                );

        return save(
                TeamOutboxType.SENDBIRD_CHANNEL_SYNC,
                payload
        );
    }

    @Transactional
    public TeamOutbox saveNotionWorkspaceSync(
            TeamWorkspaceSyncTarget target
    ) {
        NotionWorkspaceSyncPayload payload =
                new NotionWorkspaceSyncPayload(
                        target.teamId()
                );

        return save(
                TeamOutboxType.NOTION_WORKSPACE_SYNC,
                payload
        );
    }

    @Transactional
    public TeamOutbox saveExternalResourceDelete(
            Long teamPeriodId,
            TeamExternalResourceDeleteTarget target
    ) {
        ExternalResourceDeletePayload payload =
                new ExternalResourceDeletePayload(
                        teamPeriodId,
                        target.teamId(),
                        target.sendbirdChannelUrl(),
                        target.notionPageId()
                );

        return save(
                TeamOutboxType.EXTERNAL_RESOURCE_DELETE,
                payload
        );
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void markProcessing(
            Long outboxId
    ) {
        TeamOutbox outbox =
                findByIdForUpdate(
                        outboxId
                );

        if (!outbox.canRetry()) {
            return;
        }

        teamOutboxRepository.save(
                outbox.markProcessing(
                        now()
                )
        );
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void markSucceeded(
            Long outboxId
    ) {
        TeamOutbox outbox =
                findByIdForUpdate(
                        outboxId
                );

        teamOutboxRepository.save(
                outbox.markSucceeded(
                        now()
                )
        );
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void markFailed(
            Long outboxId,
            RuntimeException exception
    ) {
        TeamOutbox outbox =
                findByIdForUpdate(
                        outboxId
                );

        teamOutboxRepository.save(
                outbox.markFailed(
                        createErrorMessage(
                                exception
                        ),
                        now()
                )
        );
    }

    private TeamOutbox save(
            TeamOutboxType type,
            Object payload
    ) {
        return teamOutboxRepository.save(
                TeamOutbox.create(
                        type,
                        serialize(
                                payload
                        ),
                        now()
                )
        );
    }

    private TeamOutbox findByIdForUpdate(
            Long outboxId
    ) {
        return teamOutboxRepository.findByIdForUpdate(
                        outboxId
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.INVALID_INPUT_VALUE,
                                "Outbox 작업을 찾을 수 없습니다."
                        )
                );
    }

    private String serialize(
            Object payload
    ) {
        try {
            return objectMapper.writeValueAsString(
                    payload
            );
        } catch (JsonProcessingException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "Outbox payload 직렬화에 실패했습니다."
            );
        }
    }

    private String createErrorMessage(
            RuntimeException exception
    ) {
        if (exception == null) {
            return null;
        }

        String message =
                exception.getMessage();

        if (message == null
                || message.isBlank()) {
            return exception.getClass()
                    .getSimpleName();
        }

        return exception.getClass()
                .getSimpleName()
                + ": "
                + message;
    }

    private LocalDateTime now() {
        return LocalDateTime.now(
                clock
        );
    }
}
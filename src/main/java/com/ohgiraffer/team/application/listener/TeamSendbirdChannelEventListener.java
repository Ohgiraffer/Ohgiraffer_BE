package com.ohgiraffer.team.application.listener;

import com.ohgiraffer.team.application.event.TeamChannelSyncTarget;
import com.ohgiraffer.team.application.event.TeamConfigurationSavedEvent;
import com.ohgiraffer.team.application.service.TeamSendbirdChannelSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamSendbirdChannelEventListener {

    private final TeamSendbirdChannelSyncService teamSendbirdChannelSyncService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleTeamConfigurationSaved(
            TeamConfigurationSavedEvent event
    ) {
        event.channelSyncTargets()
                .forEach(target ->
                        syncTeamChannelSafely(
                                target,
                                event.createChatChannel()
                        )
                );
    }

    private void syncTeamChannelSafely(
            TeamChannelSyncTarget target,
            boolean createChatChannel
    ) {
        try {
            teamSendbirdChannelSyncService.sync(
                    target,
                    createChatChannel
            );
        } catch (RuntimeException exception) {
            log.error(
                    "[Team] Sendbird 팀 채널 동기화 실패 | teamId={}, memberUserIds={}, createChatChannel={}",
                    target.teamId(),
                    target.memberUserIds(),
                    createChatChannel,
                    exception
            );
        }
    }
}
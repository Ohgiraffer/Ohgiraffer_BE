package com.ohgiraffer.team.application.listener;

import com.ohgiraffer.team.application.event.TeamConfigurationSavedEvent;
import com.ohgiraffer.team.application.event.TeamWorkspaceSyncTarget;
import com.ohgiraffer.team.application.service.TeamNotionWorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamNotionWorkspaceEventListener {

    private final TeamNotionWorkspaceService teamNotionWorkspaceService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTeamConfigurationSaved(TeamConfigurationSavedEvent event) {
        if (!event.createNotionPage()) {
            return;
        }

        event.workspaceSyncTargets()
                .forEach(this::syncTeamWorkspaceSafely);
    }

    private void syncTeamWorkspaceSafely(TeamWorkspaceSyncTarget target) {
        try {
            teamNotionWorkspaceService.syncTeamWorkspace(target.teamId());
        } catch (RuntimeException exception) {
            log.error(
                    "[Team] Notion 팀 페이지 동기화 실패 | teamId={}",
                    target.teamId(),
                    exception
            );
        }
    }
}
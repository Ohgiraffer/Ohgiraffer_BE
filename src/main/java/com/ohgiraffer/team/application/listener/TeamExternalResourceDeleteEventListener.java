package com.ohgiraffer.team.application.listener;

import com.ohgiraffer.team.application.event.TeamExternalResourceDeleteTarget;
import com.ohgiraffer.team.application.event.TeamPeriodDeletedEvent;
import com.ohgiraffer.team.application.service.TeamNotionWorkspaceArchiveService;
import com.ohgiraffer.team.application.service.TeamSendbirdChannelDeleteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamExternalResourceDeleteEventListener {

    private final TeamNotionWorkspaceArchiveService teamNotionWorkspaceArchiveService;
    private final TeamSendbirdChannelDeleteService teamSendbirdChannelDeleteService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTeamPeriodDeleted(
            TeamPeriodDeletedEvent event
    ) {
        event.targets()
                .forEach(target -> deleteExternalResourceSafely(
                        event.teamPeriodId(),
                        target
                ));
    }

    private void deleteExternalResourceSafely(
            Long teamPeriodId,
            TeamExternalResourceDeleteTarget target
    ) {
        deleteSendbirdChannelSafely(
                teamPeriodId,
                target
        );

        archiveNotionPageSafely(
                teamPeriodId,
                target
        );
    }

    private void deleteSendbirdChannelSafely(
            Long teamPeriodId,
            TeamExternalResourceDeleteTarget target
    ) {
        if (target.sendbirdChannelUrl() == null
                || target.sendbirdChannelUrl().isBlank()) {
            return;
        }

        try {
            teamSendbirdChannelDeleteService.delete(
                    target.sendbirdChannelUrl()
            );
        } catch (RuntimeException exception) {
            log.error(
                    "[Team] 기간 삭제 후 Sendbird 채널 삭제 실패 | teamPeriodId={}, teamId={}, sendbirdChannelUrl={}",
                    teamPeriodId,
                    target.teamId(),
                    target.sendbirdChannelUrl(),
                    exception
            );
        }
    }

    private void archiveNotionPageSafely(
            Long teamPeriodId,
            TeamExternalResourceDeleteTarget target
    ) {
        if (target.notionPageId() == null
                || target.notionPageId().isBlank()) {
            return;
        }

        try {
            teamNotionWorkspaceArchiveService.archive(
                    target.notionPageId()
            );
        } catch (RuntimeException exception) {
            log.error(
                    "[Team] 기간 삭제 후 Notion 페이지 삭제 처리 실패 | teamPeriodId={}, teamId={}, notionPageId={}",
                    teamPeriodId,
                    target.teamId(),
                    target.notionPageId(),
                    exception
            );
        }
    }
}
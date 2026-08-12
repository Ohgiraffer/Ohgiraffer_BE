package com.ohgiraffer.team.application.listener;

import com.ohgiraffer.team.application.event.TeamPeriodDeletedEvent;
import com.ohgiraffer.team.application.service.TeamOutboxProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamExternalResourceDeleteEventListener {

    private final TeamOutboxProcessor teamOutboxProcessor;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleTeamPeriodDeleted(
            TeamPeriodDeletedEvent event
    ) {
        event.externalResourceDeleteOutboxIds()
                .forEach(this::processOutboxSafely);
    }

    private void processOutboxSafely(
            Long outboxId
    ) {
        try {
            teamOutboxProcessor.process(
                    outboxId
            );
        } catch (RuntimeException exception) {
            log.error(
                    "[TeamOutbox] 외부 리소스 삭제 즉시 처리 실패 | outboxId={}",
                    outboxId,
                    exception
            );
        }
    }
}
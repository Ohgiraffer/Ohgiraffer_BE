package com.ohgiraffer.evaluation.application.service;

import com.ohgiraffer.evaluation.application.port.StaffLookupPort;
import com.ohgiraffer.evaluation.application.usecase.EvaluationSyncNotifyUseCase;
import com.ohgiraffer.evaluation.domain.model.SheetSyncLog;
import com.ohgiraffer.evaluation.domain.repository.SheetSyncLogRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notification.domain.event.NotificationRequestedEvent;
import com.ohgiraffer.notification.domain.model.NotificationType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 동기화 결과를 운영진에게 알린다.
 *
 * <p>알림을 직접 만들지 않고 이벤트만 발행한다. 알림 도메인이 그 이벤트를 받아
 * 저장과 전송을 맡는다. 그쪽 리스너가 커밋 이후에만 동작하므로, 여기서 트랜잭션이
 * 되돌아가면 알림도 나가지 않는다.
 *
 * <p>평가 관리 화면은 운영진만 볼 수 있어 훈련생에게는 보내지 않는다.
 */
@Service
public class EvaluationSyncNotifyService implements EvaluationSyncNotifyUseCase {

    private static final String RELATED_ENTITY_TYPE = "EVALUATION_SYNC_LOG";
    private static final String TITLE = "평가 데이터가 갱신되었습니다";

    private final SheetSyncLogRepository sheetSyncLogRepository;
    private final StaffLookupPort staffLookupPort;
    private final ApplicationEventPublisher eventPublisher;

    public EvaluationSyncNotifyService(
            SheetSyncLogRepository sheetSyncLogRepository,
            StaffLookupPort staffLookupPort,
            ApplicationEventPublisher eventPublisher
    ) {
        this.sheetSyncLogRepository = sheetSyncLogRepository;
        this.staffLookupPort = staffLookupPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public int notify(Long syncLogId, Long requesterId) {
        SheetSyncLog syncLog = sheetSyncLogRepository.findById(syncLogId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.EVALUATION_SYNC_LOG_NOT_FOUND));

        /*
         * 보낸 사람은 뺀다. 방금 화면에서 요약을 보고 버튼을 누른 사람이라
         * 같은 내용을 알림으로 또 받을 이유가 없다.
         */
        List<Long> targets = staffLookupPort.findActiveStaffIds()
                .stream()
                .filter(Objects::nonNull)
                .filter(staffId -> !staffId.equals(requesterId))
                .distinct()
                .toList();

        for (Long target : targets) {
            eventPublisher.publishEvent(new NotificationRequestedEvent(
                    target,
                    NotificationType.EVALUATION,
                    TITLE,
                    syncLog.toSummaryText(),
                    RELATED_ENTITY_TYPE,
                    syncLog.getId()
            ));
        }

        return targets.size();
    }
}

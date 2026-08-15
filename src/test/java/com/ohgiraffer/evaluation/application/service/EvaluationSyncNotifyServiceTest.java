package com.ohgiraffer.evaluation.application.service;

import com.ohgiraffer.evaluation.application.port.StaffLookupPort;
import com.ohgiraffer.evaluation.domain.model.SheetSyncLog;
import com.ohgiraffer.evaluation.domain.model.TraineeChangeSummary;
import com.ohgiraffer.evaluation.domain.repository.SheetSyncLogRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notification.domain.event.NotificationRequestedEvent;
import com.ohgiraffer.notification.domain.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 누구에게 보내는지, 보낸 사람을 빼는지, 알림 도메인에 무엇을 넘기는지 확인한다.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EvaluationSyncNotifyServiceTest {

    private static final Long SYNC_LOG_ID = 1L;
    private static final Long REQUESTER_ID = 1L;
    private static final String SUMMARY = "박민준의 발표 점수가 크게 올랐습니다.";

    @Mock
    private SheetSyncLogRepository sheetSyncLogRepository;

    @Mock
    private StaffLookupPort staffLookupPort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private EvaluationSyncNotifyService evaluationSyncNotifyService;

    @BeforeEach
    void setUp() {
        evaluationSyncNotifyService = new EvaluationSyncNotifyService(
                sheetSyncLogRepository,
                staffLookupPort,
                eventPublisher
        );

        when(sheetSyncLogRepository.findById(SYNC_LOG_ID))
                .thenReturn(Optional.of(syncLog()));
        when(staffLookupPort.findActiveStaffIds())
                .thenReturn(List.of(1L, 2L, 3L));
    }

    @Test
    @DisplayName("보낸 사람을 뺀 운영진에게 알린다")
    void notifyExcludesRequester() {
        int notified = evaluationSyncNotifyService.notify(
                SYNC_LOG_ID, REQUESTER_ID);

        /*
         * 방금 화면에서 요약을 보고 버튼을 누른 사람에게 같은 내용을 또 보낼 이유가 없다.
         */
        assertEquals(2, notified);
        verify(eventPublisher, times(2)).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("알림 도메인에 이력 요약과 유형을 넘긴다")
    void notifyPublishesEvaluationEvent() {
        evaluationSyncNotifyService.notify(SYNC_LOG_ID, REQUESTER_ID);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());

        NotificationRequestedEvent event =
                (NotificationRequestedEvent) captor.getAllValues().get(0);

        assertEquals(NotificationType.EVALUATION, event.notificationType());

        /*
         * 알림은 카드를 그릴 수 없어 한 덩어리 글로 바꿔 보낸다.
         */
        assertTrue(event.content().contains("박민준"));
        assertTrue(event.content().contains("70 → 95"));
        assertEquals("EVALUATION_SYNC_LOG", event.relatedEntityType());
        assertEquals(SYNC_LOG_ID, event.relatedEntityId());
        assertNotEquals(REQUESTER_ID, event.userId());
    }

    @Test
    @DisplayName("보낼 사람이 자기밖에 없으면 아무것도 보내지 않는다")
    void notifySendsNothingWhenOnlyRequester() {
        when(staffLookupPort.findActiveStaffIds())
                .thenReturn(List.of(REQUESTER_ID));

        int notified = evaluationSyncNotifyService.notify(
                SYNC_LOG_ID, REQUESTER_ID);

        assertEquals(0, notified);
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("없는 이력으로는 알릴 수 없다")
    void notifyRequiresSyncLog() {
        when(sheetSyncLogRepository.findById(SYNC_LOG_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> evaluationSyncNotifyService.notify(
                        SYNC_LOG_ID, REQUESTER_ID)
        );

        assertEquals(
                ErrorCode.EVALUATION_SYNC_LOG_NOT_FOUND,
                exception.getErrorCode()
        );
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    private SheetSyncLog syncLog() {
        return SheetSyncLog.restore(
                SYNC_LOG_ID,
                1L,
                REQUESTER_ID,
                3,
                List.of(new TraineeChangeSummary(
                        "박민준", "중간평가", "발표", "70 → 95", "변경 없음", null)),
                Instant.parse("2026-08-10T09:31:33Z")
        );
    }
}

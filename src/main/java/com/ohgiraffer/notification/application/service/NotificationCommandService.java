package com.ohgiraffer.notification.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.sse.SseEventPublisher;
import com.ohgiraffer.notification.application.command.NotificationBulkDeleteCommand;
import com.ohgiraffer.notification.application.command.NotificationCreateCommand;
import com.ohgiraffer.notification.application.port.UserNotificationSettingPort;
import com.ohgiraffer.notification.application.result.NotificationResult;
import com.ohgiraffer.notification.application.usecase.NotificationCommandUseCase;
import com.ohgiraffer.notification.domain.model.Notification;
import com.ohgiraffer.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * comment.
 *  NotificationCommandUseCase 구현체
 *  상태 변경만 담당 - 저장/읽음처리/삭제
 *  본인 소유 검증은 findById 후 도메인의 isOwnedBy로 위임
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCommandService implements NotificationCommandUseCase {

    private final NotificationRepository notificationRepository;
    private final SseEventPublisher sseEventPublisher;
    private final UserNotificationSettingPort userNotificationSettingPort;

    // 알림 생성 - row는 항상 저장, push는 notificationOn=true일 때만
    @Override
    @Transactional
    public NotificationResult create(NotificationCreateCommand command) {
        Notification notification = Notification.create(
                command.userId(),
                command.notificationType(),
                command.title(),
                command.content(),
                command.relatedEntityType(),
                command.relatedEntityId()
        );

        Notification saved = notificationRepository.save(notification);
        NotificationResult result = NotificationResult.from(saved);

        if (userNotificationSettingPort.isNotificationOn(command.userId())) {
            sseEventPublisher.publish(command.userId(), "notification", result);
        }

        log.info("[Notification] 알림 생성 완료 | userId={}, type={}, notificationId={}",
                command.userId(), command.notificationType(), saved.getNotificationId());

        return result;
    }

    // 읽음 처리
    @Override
    @Transactional
    public NotificationResult markAsRead(Long notificationId, Long requesterId) {
        Notification notification = findOwnedNotification(notificationId, requesterId);

        notification.markAsRead();
        Notification saved = notificationRepository.save(notification);

        log.info("[Notification] 읽음 처리 완료 | notificationId={}, userId={}", notificationId, requesterId);

        return NotificationResult.from(saved);
    }

    // 개별 삭제- 하드 딜리트
    @Override
    @Transactional
    public void delete(Long notificationId, Long requesterId) {
        findOwnedNotification(notificationId, requesterId);
        notificationRepository.deleteById(notificationId);

        log.info("[Notification] 개별 삭제 완료 | notificationId={}, userId={}", notificationId, requesterId);
    }

    // 선택 삭제 - 본인 소유 아닌 id는 조용히 스킵되고 실제 삭제된 건수만 반환
    @Override
    @Transactional
    public long bulkDelete(NotificationBulkDeleteCommand command) {
        List<Long> notificationIds = command.notificationIds();

        if (notificationIds == null || notificationIds.isEmpty()) {
            throw new BusinessException(ErrorCode.NOTI_IDS_REQUIRED);
        }

        long deletedCount = notificationRepository.deleteByIdsAndUserId(notificationIds, command.requesterId());

        log.info("[Notification] 선택 삭제 완료 | userId={}, requested={}, deleted={}",
                command.requesterId(), notificationIds.size(), deletedCount);

        return deletedCount;
    }


    // 단건 조회 + 본인 소유 검증 공통 로직
    private Notification findOwnedNotification(Long notificationId, Long requesterId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTI_NOT_FOUND));

        if (!notification.isOwnedBy(requesterId)) {
            throw new BusinessException(ErrorCode.NOTI_ACCESS_DENIED);
        }

        return notification;
    }

}

package com.ohgiraffer.notification.application.service;

import com.ohgiraffer.notification.application.result.NotificationResult;
import com.ohgiraffer.notification.application.usecase.NotificationQueryUseCase;
import com.ohgiraffer.notification.domain.model.NotificationType;
import com.ohgiraffer.notification.domain.repository.NotificationRepository;
import com.ohgiraffer.notification.domain.repository.NotificationSearchCondition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * comment.
 *  NotificationQueryUseCase 구현체
 *  조회 전용이라 readOnly 트랜잭션으로 처리
 */

@Service
@Transactional(readOnly = true)
public class NotificationQueryService implements NotificationQueryUseCase {

    private final NotificationRepository notificationRepository;

    public NotificationQueryService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // 목록 조회 - 검색조건 조립 후 리포지토리에 위임, 결과를 응답 형태로 변환
    @Override
    public List<NotificationResult> getNotifications(Long userId, Boolean isRead, NotificationType notificationType) {
        NotificationSearchCondition condition = new NotificationSearchCondition(userId, isRead, notificationType);

        return notificationRepository.search(condition)
                .stream()
                .map(NotificationResult::from)
                .toList();
    }

}

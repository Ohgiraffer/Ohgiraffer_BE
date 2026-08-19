package com.ohgiraffer.attendance.infrastructure.adapter;

import com.ohgiraffer.attendance.application.port.NotifiedTodayCheckPort;
import com.ohgiraffer.notification.domain.model.NotificationType;
import com.ohgiraffer.notification.domain.repository.NotificationRepository;
import com.ohgiraffer.notification.domain.repository.NotificationSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/*
 * comment.
 *  NotifiedTodayCheckPort 구현체.
 *  전용 날짜조건 쿼리를 새로 만들지 않고, 기존 NotificationRepository.search()로
 *  유저+타입 조건만 걸어 가져온 뒤 오늘 생성분이 있는지 애플리케이션 레이어에서 필터링함.
 *  현재 규모(소수 인원 부트캠프)에서는 이 방식이 충분히 안전함.
 */
@Component
@RequiredArgsConstructor
public class NotifiedTodayCheckAdapter implements NotifiedTodayCheckPort {

    private final NotificationRepository notificationRepository;

    @Override
    public boolean isNotifiedToday(Long userId, NotificationType notificationType) {
        LocalDate today = LocalDate.now();

        return notificationRepository.search(
                        new NotificationSearchCondition(userId, null, notificationType)
                ).stream()
                .anyMatch(n -> isToday(n.getCreatedAt(), today));
    }

    private boolean isToday(Instant createdAt, LocalDate today) {
        return createdAt != null
                && createdAt.atZone(ZoneId.systemDefault()).toLocalDate().equals(today);
    }

}

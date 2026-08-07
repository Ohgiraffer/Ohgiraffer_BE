package com.ohgiraffer.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationJpaRepository
        extends JpaRepository<NotificationJpaEntity, Long>, NotificationQuerydslRepository {

    // 선택 삭제 시 본인 소유 여부까지 한 번에 검증하며 삭제 - id 목록 중 실제 삭제된 개수만 반환
    long deleteByIdInAndUserId(List<Long> notificationIds, Long userId);

}

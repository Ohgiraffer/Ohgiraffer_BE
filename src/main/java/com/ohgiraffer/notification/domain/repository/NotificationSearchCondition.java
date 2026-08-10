package com.ohgiraffer.notification.domain.repository;

import com.ohgiraffer.notification.domain.model.NotificationType;

/*
 * comment.
 *  목록 조회 동적 검색 조건
 *  isRead/notificationType은 쿼리파라미터 미전달 시 null로 넘어와 필터 없이 전체 조회됨
 */

public record NotificationSearchCondition(
        Long userId,
        Boolean isRead,
        NotificationType notificationType
) {
}

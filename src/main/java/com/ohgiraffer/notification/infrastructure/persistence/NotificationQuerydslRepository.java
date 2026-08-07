package com.ohgiraffer.notification.infrastructure.persistence;

import com.ohgiraffer.notification.domain.model.Notification;
import com.ohgiraffer.notification.domain.repository.NotificationSearchCondition;

import java.util.List;

/*
 * comment.
 *  목록 조회용 동적 쿼리 커스텀 인터페이스
 *  페이지네이션 없음 - 프론트가 전체를 받아 자체 스크롤 처리하기로 확정됨
 */

public interface NotificationQuerydslRepository {

    // isRead/notificationType 조건별 동적 필터 + createdAt 최신순 정렬, 페이징 없이 전체 반환
    List<Notification> search(NotificationSearchCondition condition);

    // 안읽음 개수 조회 - COUNT 단일 쿼리
    long countUnread(Long userId);


}

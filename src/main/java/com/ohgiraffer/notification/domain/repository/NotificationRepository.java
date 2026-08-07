package com.ohgiraffer.notification.domain.repository;

import com.ohgiraffer.notification.domain.model.Notification;

import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  알림 도메인 저장소 인터페이스
 *  domain 계층은 이 인터페이스만 알고, 실제 구현(JPA)은 infrastructure에서 어댑터로 주입됨
 */

public interface NotificationRepository {

    // 신규 저장 / 읽음 처리 후 갱신 공용
    Notification save(Notification notification);

    // 단건 조회 - 읽음 처리/개별 삭제 전 본인 확인용
    Optional<Notification> findById(Long notificationId);

    // 목록 조회 - 필터 조건 기반 동적 조회, 페이징 없음
    List<Notification> search(NotificationSearchCondition condition);

    // 개별 삭제 - 하드 딜리트
    void deleteById(Long notificationId);

    // 선택 삭제 - 본인 소유 검증까지 포함해서 삭제, 실제 삭제된 개수 반환
    long deleteByIdsAndUserId(List<Long> notificationIds, Long userId);

}

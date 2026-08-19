package com.ohgiraffer.notification.infrastructure.adapter;

import com.ohgiraffer.notification.domain.model.Notification;
import com.ohgiraffer.notification.domain.repository.NotificationRepository;
import com.ohgiraffer.notification.domain.repository.NotificationSearchCondition;
import com.ohgiraffer.notification.infrastructure.persistence.NotificationJpaEntity;
import com.ohgiraffer.notification.infrastructure.persistence.NotificationJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  domain/repository의 NotificationRepository 인터페이스 구현체
 *  실제 JPA 저장소(NotificationJpaRepository)에 위임만 하고,
 *  domain 계층이 Spring Data JPA에 직접 의존하지 않도록 어댑터로 분리
 */

@Repository
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;

    public NotificationRepositoryAdapter(NotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // 신규 저장 / 읽음 처리 후 갱신 공용
    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = NotificationJpaEntity.from(notification);
        NotificationJpaEntity saved = jpaRepository.saveAndFlush(entity);
        return saved.toDomain();
    }

    // 단건 조회 - 본인 확인 후 처리해야 하는 NOTI-002/003에서 사용
    @Override
    public Optional<Notification> findById(Long notificationId) {
        return jpaRepository.findById(notificationId).map(NotificationJpaEntity::toDomain);
    }

    // 목록 조회 - Querydsl 동적 필터 위임
    @Override
    public List<Notification> search(NotificationSearchCondition condition) {
        return jpaRepository.search(condition);
    }

    // 개별 삭제 - 하드 딜리트
    @Override
    public void deleteById(Long notificationId) {
        jpaRepository.deleteById(notificationId);
    }

    // 선택 삭제 - 본인 소유 id만 삭제되도록 쿼리 자체에서 userId까지 조건에 포함
    @Override
    public long deleteByIdsAndUserId(List<Long> notificationIds, Long userId) {
        return jpaRepository.deleteByIdInAndUserId(notificationIds, userId);
    }

    // 안읽음 개수 조회 - Querydsl count 위임
    @Override
    public long countUnread(Long userId) {
        return jpaRepository.countUnread(userId);
    }

}

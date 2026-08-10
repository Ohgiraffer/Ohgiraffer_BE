package com.ohgiraffer.notification.infrastructure.persistence;

import com.ohgiraffer.notification.domain.model.Notification;
import com.ohgiraffer.notification.domain.model.NotificationType;
import com.ohgiraffer.notification.domain.repository.NotificationSearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.List;

import static com.ohgiraffer.notification.infrastructure.persistence.QNotificationJpaEntity.notificationJpaEntity;

public class NotificationQuerydslRepositoryImpl implements NotificationQuerydslRepository  {

    private final JPAQueryFactory queryFactory;

    public NotificationQuerydslRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    // 목록 조회 - userId는 필수 조건, isRead/type은 선택 필터
    @Override
    public List<Notification> search(NotificationSearchCondition condition) {
        return queryFactory
                .selectFrom(notificationJpaEntity)
                .where(
                        userIdEq(condition.userId()),
                        isReadEq(condition.isRead()),
                        typeEq(condition.notificationType())
                )
                .orderBy(notificationJpaEntity.createdAt.desc())
                .fetch()
                .stream()
                .map(NotificationJpaEntity::toDomain)
                .toList();
    }

    // 안읽음 개수 조회 - 목록 조회와 별개로 필터 없이 isRead=false 고정
    @Override
    public long countUnread(Long userId) {
        Long count = queryFactory
                .select(notificationJpaEntity.count())
                .from(notificationJpaEntity)
                .where(
                        notificationJpaEntity.userId.eq(userId),
                        notificationJpaEntity.isRead.eq(false)
                )
                .fetchOne();

        return count != null ? count : 0L;
    }

    // 본인 알림만 조회 - userId는 항상 존재해야 하는 필수 조건
    private BooleanExpression userIdEq(Long userId) {
        return notificationJpaEntity.userId.eq(userId);
    }

    // 읽음 여부 필터 - null이면 조건 없이 전체
    private BooleanExpression isReadEq(Boolean isRead) {
        return isRead != null ? notificationJpaEntity.isRead.eq(isRead) : null;
    }

    // 알림 유형 필터 - null이면 조건 없이 전체
    private BooleanExpression typeEq(NotificationType notificationType) {
        return notificationType != null ? notificationJpaEntity.notificationType.eq(notificationType) : null;
    }

}

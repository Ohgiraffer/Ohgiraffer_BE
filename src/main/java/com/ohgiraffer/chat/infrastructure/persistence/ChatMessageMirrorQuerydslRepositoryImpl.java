package com.ohgiraffer.chat.infrastructure.persistence;

import com.ohgiraffer.chat.domain.repository.ChatMessageSearchCondition;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.ohgiraffer.chat.infrastructure.persistence.QChatMessageMirrorJpaEntity.chatMessageMirrorJpaEntity;
/*
 * comment.
 *  통합 검색 Querydsl 구현체
 *  조건이 null이면 해당 필터는 적용하지 않는 방식(동적 쿼리)으로 BooleanBuilder 사용
 *  삭제된 메시지는 검색 결과에서 항상 제외
 */

public class ChatMessageMirrorQuerydslRepositoryImpl implements ChatMessageMirrorQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    public ChatMessageMirrorQuerydslRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<ChatMessageMirrorJpaEntity> search(ChatMessageSearchCondition condition, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        // 채널 필터
        if (condition.channelId() != null) {
            builder.and(chatMessageMirrorJpaEntity.channelId.eq(condition.channelId()));
        }
        // 작성자 필터
        if (condition.senderId() != null) {
            builder.and(chatMessageMirrorJpaEntity.senderId.eq(condition.senderId()));
        }
        // 키워드 필터 (대소문자 무시 부분일치)
        if (condition.keyword() != null && !condition.keyword().isBlank()) {
            builder.and(chatMessageMirrorJpaEntity.content.containsIgnoreCase(condition.keyword()));
        }
        // 기간 필터 (시작일)
        if (condition.startDate() != null) {
            builder.and(chatMessageMirrorJpaEntity.sentAt.goe(condition.startDate()));
        }
        // 기간 필터 (종료일)
        if (condition.endDate() != null) {
            builder.and(chatMessageMirrorJpaEntity.sentAt.loe(condition.endDate()));
        }
        // 삭제된 메시지는 항상 제외
        builder.and(chatMessageMirrorJpaEntity.deletedAt.isNull());

        List<ChatMessageMirrorJpaEntity> content = queryFactory
                .selectFrom(chatMessageMirrorJpaEntity)
                .where(builder)
                .orderBy(chatMessageMirrorJpaEntity.sentAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // count 쿼리는 조회 조건과 별도로 실행 (페이징 전체 개수 계산용)
        Long total = queryFactory
                .select(chatMessageMirrorJpaEntity.count())
                .from(chatMessageMirrorJpaEntity)
                .where(builder)
                .fetchOne();

        return PageableExecutionUtils.getPage(content, pageable, () -> total == null ? 0 : total);
    }

}

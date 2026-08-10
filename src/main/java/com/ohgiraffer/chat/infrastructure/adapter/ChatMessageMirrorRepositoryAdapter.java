package com.ohgiraffer.chat.infrastructure.adapter;

import com.ohgiraffer.chat.domain.model.ChatMessageMirror;
import com.ohgiraffer.chat.domain.repository.ChannelLastMessage;
import com.ohgiraffer.chat.domain.repository.ChatMessageMirrorRepository;
import com.ohgiraffer.chat.domain.repository.ChatMessageSearchCondition;
import com.ohgiraffer.chat.infrastructure.persistence.ChatMessageMirrorJpaEntity;
import com.ohgiraffer.chat.infrastructure.persistence.ChatMessageMirrorJpaRepository;
import com.ohgiraffer.chat.infrastructure.projection.ChannelLastMessageProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * comment.
 *  domain/repository의 ChatMessageMirrorRepository 인터페이스 구현체
 *  실제 JPA 저장소(ChatMessageMirrorJpaRepository)에 위임만 하고,
 *  domain 계층이 Spring Data JPA에 직접 의존하지 않도록 어댑터로 분리
 */

@Repository
public class ChatMessageMirrorRepositoryAdapter implements ChatMessageMirrorRepository {

    private final ChatMessageMirrorJpaRepository jpaRepository;

    public ChatMessageMirrorRepositoryAdapter(ChatMessageMirrorJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // 신규 메시지 저장 / 기존 메시지 갱신 공용
    @Override
    public ChatMessageMirror save(ChatMessageMirror message) {
        ChatMessageMirrorJpaEntity entity = ChatMessageMirrorJpaEntity.from(message);
        return jpaRepository.save(entity).toDomain();
    }

    // 메시지 단건 조회 - 수정/삭제 시 본인 확인용
    @Override
    public Optional<ChatMessageMirror> findById(Long chatMessageId) {
        return jpaRepository.findById(chatMessageId).map(ChatMessageMirrorJpaEntity::toDomain);
    }

    // 웹훅 멱등성 체크용 단건 조회
    @Override
    public Optional<ChatMessageMirror> findBySendbirdMessageId(String sendbirdMessageId) {
        return jpaRepository.findBySendbirdMessageId(sendbirdMessageId).map(ChatMessageMirrorJpaEntity::toDomain);
    }

    // 웹훅 중복 이벤트 여부 빠르게 확인
    @Override
    public boolean existsBySendbirdMessageId(String sendbirdMessageId) {
        return jpaRepository.existsBySendbirdMessageId(sendbirdMessageId);
    }

    // 채널 메시지 이력 조회 - 최신순, 삭제 제외
    @Override
    public Page<ChatMessageMirror> findByChannelIdOrderBySentAtDesc(String channelId, Pageable pageable) {
        return jpaRepository.findByChannelIdAndDeletedAtIsNullOrderBySentAtDescIdDesc(channelId, pageable)
                .map(ChatMessageMirrorJpaEntity::toDomain);
    }

    // 스레드 답글 목록 조회 - 삭제 제외
    @Override
    public Page<ChatMessageMirror> findByParentMessageId(Long parentMessageId, Pageable pageable) {
        return jpaRepository.findByParentMessageIdAndDeletedAtIsNullOrderBySentAtDescIdDesc(parentMessageId, pageable)
                .map(ChatMessageMirrorJpaEntity::toDomain);
    }

    // 원본 메시지의 답글 수 카운트 - 삭제 제외
    @Override
    public long countByParentMessageId(Long parentMessageId) {
        return jpaRepository.countByParentMessageIdAndDeletedAtIsNull(parentMessageId);
    }

    // 통합 검색 - Querydsl 동적쿼리 결과를 Domain 페이지로 변환
    @Override
    public Page<ChatMessageMirror> search(ChatMessageSearchCondition condition, Pageable pageable) {
        return jpaRepository.search(condition, pageable).map(ChatMessageMirrorJpaEntity::toDomain);
    }

    // 채널별 최신메시지 1건 일괄 조회 - 윈도우함수 네이티브쿼리 결과(Projection)를 Map으로 변환
    @Override
    public Map<String, ChannelLastMessage> findLatestMessagesByChannelIds(List<String> channelIds) {
        return jpaRepository.findLatestMessagesByChannelIds(channelIds).stream()
                .collect(Collectors.toMap(
                        ChannelLastMessageProjection::getChannelId,
                        p -> new ChannelLastMessage(p.getContent(), p.getSentAt())
                ));
    }

    // 채널 최신 메시지 단건 조회 - 상세조회에서 최신메시지 id만 필요할 때 전체 이력 대신 이걸 씀
    @Override
    public Optional<ChatMessageMirror> findTopByChannelIdOrderBySentAtDesc(String channelId) {
        return jpaRepository.findTopByChannelIdAndDeletedAtIsNullOrderBySentAtDescIdDesc(channelId)
                .map(ChatMessageMirrorJpaEntity::toDomain);
    }

    // 즉시 flush하여 제약 위반 예외를 호출부(ChatMessageMirrorSaver)에서 그 자리에 바로 잡을 수 있게 함
    @Override
    public ChatMessageMirror saveAndFlush(ChatMessageMirror message) {
        ChatMessageMirrorJpaEntity entity = ChatMessageMirrorJpaEntity.from(message);
        return jpaRepository.saveAndFlush(entity).toDomain();
    }

    @Override
    public long countByParentMessageIdAndDeletedAtIsNull(Long parentMessageId) {
        return jpaRepository.countByParentMessageIdAndDeletedAtIsNull(parentMessageId);
    }

}
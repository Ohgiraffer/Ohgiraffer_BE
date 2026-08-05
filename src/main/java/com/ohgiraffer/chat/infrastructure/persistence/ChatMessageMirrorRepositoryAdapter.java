package com.ohgiraffer.chat.infrastructure.persistence;

import com.ohgiraffer.chat.domain.model.ChatMessageMirror;
import com.ohgiraffer.chat.domain.repository.ChatMessageMirrorRepository;
import com.ohgiraffer.chat.domain.repository.ChatMessageSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Override
    public ChatMessageMirror save(ChatMessageMirror message) {
        ChatMessageMirrorJpaEntity entity = ChatMessageMirrorJpaEntity.from(message);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<ChatMessageMirror> findById(Long chatMessageId) {
        return jpaRepository.findById(chatMessageId).map(ChatMessageMirrorJpaEntity::toDomain);
    }

    @Override
    public Optional<ChatMessageMirror> findBySendbirdMessageId(String sendbirdMessageId) {
        return jpaRepository.findBySendbirdMessageId(sendbirdMessageId).map(ChatMessageMirrorJpaEntity::toDomain);
    }

    @Override
    public boolean existsBySendbirdMessageId(String sendbirdMessageId) {
        return jpaRepository.existsBySendbirdMessageId(sendbirdMessageId);
    }

    @Override
    public List<ChatMessageMirror> findByChannelIdOrderBySentAtDesc(String channelId) {
        return jpaRepository.findByChannelIdAndDeletedAtIsNullOrderBySentAtDesc(channelId)
                .stream().map(ChatMessageMirrorJpaEntity::toDomain).toList();
    }

    @Override
    public List<ChatMessageMirror> findByParentMessageId(Long parentMessageId) {
        return jpaRepository.findByParentMessageIdAndDeletedAtIsNull(parentMessageId)
                .stream().map(ChatMessageMirrorJpaEntity::toDomain).toList();
    }

    @Override
    public long countByParentMessageId(Long parentMessageId) {
        return jpaRepository.countByParentMessageIdAndDeletedAtIsNull(parentMessageId);
    }

    @Override
    public Page<ChatMessageMirror> search(ChatMessageSearchCondition condition, Pageable pageable) {
        return jpaRepository.search(condition, pageable).map(ChatMessageMirrorJpaEntity::toDomain);
    }
}
package com.ohgiraffer.chat.infrastructure.persistence;

import com.ohgiraffer.chat.domain.model.ChatMessageMirror;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  Spring Data JPA가 구현체를 자동으로 생성
 *  -> Domain을 모르고 JpaEntity만 다룸
 */

public interface ChatMessageMirrorJpaRepository
        extends JpaRepository<ChatMessageMirrorJpaEntity, Long>, ChatMessageMirrorQuerydslRepository {

    // 웹훅 멱등성 체크용
    Optional<ChatMessageMirrorJpaEntity> findBySendbirdMessageId(String sendbirdMessageId);
    boolean existsBySendbirdMessageId(String sendbirdMessageId);

    // 채널 메시지 이력 조회, 삭제된 건 제외
    List<ChatMessageMirrorJpaEntity> findByChannelIdAndDeletedAtIsNullOrderBySentAtDesc(String channelId);

    // 스레드 답글 조회, 삭제된 건 제외
    List<ChatMessageMirrorJpaEntity> findByParentMessageIdAndDeletedAtIsNull(Long parentMessageId);

    // 원본 메시지 답글 수 카운트, 삭제된 건 제외
    long countByParentMessageIdAndDeletedAtIsNull(Long parentMessageId);

}

package com.ohgiraffer.chat.infrastructure.persistence;

import com.ohgiraffer.chat.domain.model.ChatMessageMirror;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    Page<ChatMessageMirrorJpaEntity> findByChannelIdAndDeletedAtIsNullOrderBySentAtDescIdDesc(String channelId, Pageable pageable);

    // 스레드 답글 조회, 삭제된 건 제외
    Page<ChatMessageMirrorJpaEntity> findByParentMessageIdAndDeletedAtIsNullOrderBySentAtDescIdDesc(Long parentMessageId, Pageable pageable);

    // 원본 메시지 답글 수 카운트, 삭제된 건 제외
    long countByParentMessageIdAndDeletedAtIsNull(Long parentMessageId);

    // 채널별 최신메시지 1건 - Greatest-N-per-Group을 윈도우 함수로 처리
    @org.springframework.data.jpa.repository.Query(value = """
            SELECT ranked.channel_id AS channelId, ranked.content AS content, ranked.sent_at AS sentAt
            FROM (
                SELECT channel_id, content, sent_at,
                       ROW_NUMBER() OVER (PARTITION BY channel_id ORDER BY chat_message_id DESC) AS rn
                FROM chat_message_mirror
                WHERE channel_id IN (:channelIds) AND parent_message_id IS NULL AND deleted_at IS NULL
            ) ranked
            WHERE ranked.rn = 1
            """, nativeQuery = true)
    List<ChannelLastMessageProjection> findLatestMessagesByChannelIds(@org.springframework.data.repository.query.Param("channelIds") List<String> channelIds);

    // 채널 최신 메시지 1건 - Spring Data 'Top' 키워드로 LIMIT 1 자동 적용
    Optional<ChatMessageMirrorJpaEntity> findTopByChannelIdAndDeletedAtIsNullOrderBySentAtDesc(String channelId);

}

package com.ohgiraffer.chat.domain.repository;

import com.ohgiraffer.chat.domain.model.ChatMessageMirror;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  채팅 메시지 미러링 저장소 인터페이스
 *  Sendbird 웹훅으로 수신한 메시지를 우리 DB에 미러링/조회하는 계약을 정의함
 *  구현체(infrastructure/persistence)는 이 인터페이스에만 의존하도록 도메인 계층에 위치
 */

public interface ChatMessageMirrorRepository {

    ChatMessageMirror save(ChatMessageMirror message);

    // 메시지 단건 조회 - 수정/삭제 시 본인 확인용
    Optional<ChatMessageMirror> findById(Long chatMessageId);

    // 웹훅 멱등성 체크용 - 이미 처리된 이벤트인지 확인
    Optional<ChatMessageMirror> findBySendbirdMessageId(String sendbirdMessageId);
    boolean existsBySendbirdMessageId(String sendbirdMessageId);

    // 채널 메시지 이력 조회, 삭제된 메시지는 제외
    List<ChatMessageMirror> findByChannelIdOrderBySentAtDesc(String channelId);

    // 스레드 답글 목록 조회
    List<ChatMessageMirror> findByParentMessageId(Long parentMessageId);

    // 원본 메시지의 답글 수 카운트
    long countByParentMessageId(Long parentMessageId);

    // 통합 검색 - 조건 조합은 Querydsl로 동적 처리
    Page<ChatMessageMirror> search(ChatMessageSearchCondition condition, Pageable pageable);

}

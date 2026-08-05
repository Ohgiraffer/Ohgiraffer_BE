package com.ohgiraffer.chat.application.service;

import com.ohgiraffer.chat.application.result.ChatMessageResult;
import com.ohgiraffer.chat.application.usecase.ChatMessageMirrorQueryUseCase;
import com.ohgiraffer.chat.domain.repository.ChatMessageMirrorRepository;
import com.ohgiraffer.chat.domain.repository.ChatMessageSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * comment.
 *  ChatMessageQueryUseCase 구현체
 *  조회 전용이라 @Transactional(readOnly = true)로 처리
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageMirrorMirrorQueryService implements ChatMessageMirrorQueryUseCase {

    private final ChatMessageMirrorRepository chatMessageMirrorRepository;

    //  채널 메시지 이력 조회 - 최신순 정렬된 도메인 리스트를 응답용 Result로 변환
    @Override
    public List<ChatMessageResult> getChannelMessages(String channelId) {
        return chatMessageMirrorRepository.findByChannelIdOrderBySentAtDesc(channelId)
                .stream().map(ChatMessageResult::from).toList();
    }

    // 스레드 답글 조회 - 원본 메시지 id(parentMessageId) 기준 답글 목록
    @Override
    public List<ChatMessageResult> getThreadReplies(Long parentMessageId) {
        return chatMessageMirrorRepository.findByParentMessageId(parentMessageId)
                .stream().map(ChatMessageResult::from).toList();
    }

    // 메시지 통합 검색 - 채널/작성자/키워드/기간 조건 조합, Querydsl 동적쿼리 결과를 Result 페이지로 변환
    @Override
    public Page<ChatMessageResult> searchMessages(ChatMessageSearchCondition condition, Pageable pageable) {
        return chatMessageMirrorRepository.search(condition, pageable).map(ChatMessageResult::from);
    }

}

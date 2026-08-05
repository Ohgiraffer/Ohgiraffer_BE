package com.ohgiraffer.chat.infrastructure.persistence;

import com.ohgiraffer.chat.domain.model.ChatMessageMirror;
import com.ohgiraffer.chat.domain.repository.ChatMessageSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/*
 * comment.
 *  Querydsl 커스텀 저장소 인터페이스
 *  Spring Data가 자동 인식하려면 구현체 클래스명이 반드시 "인터페이스명 + Impl"이어야 함
 *  -> ChatMessageMirrorJpaRepository가 이 인터페이스를 상속하고, 구현체는 ChatMessageMirrorQuerydslRepositoryImpl로 작성
 */

public interface ChatMessageMirrorQuerydslRepository {

    // 통합 검색 - 채널/작성자/키워드/기간 조건 동적 조합
    Page<ChatMessageMirrorJpaEntity> search(ChatMessageSearchCondition condition, Pageable pageable);

}

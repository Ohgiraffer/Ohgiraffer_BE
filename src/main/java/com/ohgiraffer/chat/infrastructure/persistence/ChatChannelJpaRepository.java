package com.ohgiraffer.chat.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  Spring Data JPA가 구현체를 자동으로 생성
 *  -> Domain을 모르고 JpaEntity만 다룸
 */

public interface ChatChannelJpaRepository extends JpaRepository<ChatChannelJpaEntity, Long> {

    // 웹훅/멤버갱신 시 채널 식별용 - Spring Data 이름 기반 자동구현
    Optional<ChatChannelJpaEntity> findBySendbirdChannelUrl(String sendbirdChannelUrl);

    // 팀변경 시 대상 채널 조회 - Spring Data 이름 기반 자동구현
    List<ChatChannelJpaEntity> findAllByTeamId(Long teamId);

    // 통합검색 결과에 섞인 여러 채널을 벌크 조회 - Spring Data 이름 기반 자동구현
    List<ChatChannelJpaEntity> findAllBySendbirdChannelUrlIn(List<String> sendbirdChannelUrls);

}

package com.ohgiraffer.chat.infrastructure.adapter;

import com.ohgiraffer.chat.domain.model.ChatChannel;
import com.ohgiraffer.chat.domain.repository.ChatChannelRepository;
import com.ohgiraffer.chat.infrastructure.persistence.ChatChannelJpaEntity;
import com.ohgiraffer.chat.infrastructure.persistence.ChatChannelJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  domain/repository의 ChatChannelRepository 구현체
 *  Domain <-> JpaEntity 변환을 이 클래스가 전담함
 */

@Repository
public class ChatChannelRepositoryAdapter implements ChatChannelRepository {

    private final ChatChannelJpaRepository jpaRepository;

    public ChatChannelRepositoryAdapter(ChatChannelJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // 신규 채널 저장 / 기존 채널 갱신 공용
    @Override
    public ChatChannel save(ChatChannel channel) {
        return jpaRepository.save(ChatChannelJpaEntity.from(channel)).toDomain();
    }

    // PK 기준 단건 조회
    @Override
    public Optional<ChatChannel> findById(Long id) {
        return jpaRepository.findById(id).map(ChatChannelJpaEntity::toDomain);
    }

    // Sendbird 채널 URL로 우리 DB 레코드 찾음 - 웹훅/멤버갱신 시 사용
    @Override
    public Optional<ChatChannel> findBySendbirdChannelUrl(String sendbirdChannelUrl) {
        return jpaRepository.findBySendbirdChannelUrl(sendbirdChannelUrl).map(ChatChannelJpaEntity::toDomain);
    }

    // 팀변경 시 대상 채널 조회
    @Override
    public List<ChatChannel> findAllByTeamId(Long teamId) {
        return jpaRepository.findAllByTeamId(teamId).stream().map(ChatChannelJpaEntity::toDomain).toList();
    }

    // channelId 목록 IN절 일괄 조회 - JpaRepository가 기본 상속하는 findAllById 그대로 사용
    @Override
    public List<ChatChannel> findAllByIdIn(List<Long> ids) {
        return jpaRepository.findAllById(ids).stream().map(ChatChannelJpaEntity::toDomain).toList();
    }

    @Override
    public List<ChatChannel> findAllBySendbirdChannelUrlIn(List<String> sendbirdChannelUrls) {
        return jpaRepository.findAllBySendbirdChannelUrlIn(sendbirdChannelUrls).stream()
                .map(ChatChannelJpaEntity::toDomain) // 실제 변환 메서드명은 파일 확인 필요
                .toList();
    }

}

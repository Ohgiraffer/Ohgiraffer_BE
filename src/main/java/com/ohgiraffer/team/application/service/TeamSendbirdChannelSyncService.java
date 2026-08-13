package com.ohgiraffer.team.application.service;

import com.ohgiraffer.chat.application.command.UpdateChannelMembersCommand;
import com.ohgiraffer.chat.application.usecase.ChatChannelCommandUseCase;
import com.ohgiraffer.chat.domain.model.ChatChannel;
import com.ohgiraffer.chat.domain.model.ChatChannelMember;
import com.ohgiraffer.chat.domain.repository.ChatChannelMemberRepository;
import com.ohgiraffer.chat.domain.repository.ChatChannelRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.team.application.event.TeamChannelSyncTarget;
import com.ohgiraffer.team.domain.model.Team;
import com.ohgiraffer.team.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamSendbirdChannelSyncService {

    private final TeamRepository teamRepository;
    private final ChatChannelCommandUseCase chatChannelCommandUseCase;
    private final ChatChannelRepository chatChannelRepository;
    private final ChatChannelMemberRepository chatChannelMemberRepository;

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void sync(
            TeamChannelSyncTarget target,
            boolean createChatChannel
    ) {
        Team team =
                lockTeam(
                        target.teamId()
                );

        ChatChannel teamChannel =
                findUniqueTeamChannel(
                        target.teamId()
                );

        if (teamChannel == null) {
            createTeamChannelIfAllowed(
                    team,
                    target,
                    createChatChannel
            );
            return;
        }

        updateTeamChannelMembers(
                teamChannel,
                target.memberUserIds()
        );
    }

    private Team lockTeam(
            Long teamId
    ) {
        return teamRepository.findByIdForUpdate(
                        teamId
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.TEAM_NOT_FOUND
                        )
                );
    }

    private ChatChannel findUniqueTeamChannel(
            Long teamId
    ) {
        List<ChatChannel> teamChannels =
                chatChannelRepository.findAllByTeamId(
                        teamId
                );

        if (teamChannels.size() > 1) {
            throw new IllegalStateException(
                    "팀 채널이 중복 생성되었습니다. teamId=" + teamId
                            + ", channelCount=" + teamChannels.size()
            );
        }

        return teamChannels.stream()
                .findFirst()
                .orElse(
                        null
                );
    }

    private void createTeamChannelIfAllowed(
            Team team,
            TeamChannelSyncTarget target,
            boolean createChatChannel
    ) {
        if (!createChatChannel) {
            log.info(
                    "[Team] 팀 채널 생성 생략 - 생성 옵션 미선택 | teamId={}",
                    target.teamId()
            );
            return;
        }

        if (target.memberUserIds()
                .isEmpty()) {
            log.info(
                    "[Team] 팀 채널 생성 생략 - 멤버 없음 | teamId={}",
                    target.teamId()
            );
            return;
        }

        chatChannelCommandUseCase.createTeamChannel(
                team.getId(),
                team.getName(),
                target.memberUserIds()
        );
    }

    private void updateTeamChannelMembers(
            ChatChannel teamChannel,
            List<Long> targetMemberUserIds
    ) {
        Set<Long> currentMemberUserIds =
                findCurrentMemberUserIds(
                        teamChannel.getId()
                );

        Set<Long> targetMemberUserIdSet =
                new HashSet<>(
                        targetMemberUserIds
                );

        List<Long> addUserIds =
                targetMemberUserIdSet.stream()
                        .filter(userId -> !currentMemberUserIds.contains(
                                userId
                        ))
                        .toList();

        List<Long> removeUserIds =
                currentMemberUserIds.stream()
                        .filter(userId -> !targetMemberUserIdSet.contains(
                                userId
                        ))
                        .toList();

        if (addUserIds.isEmpty()
                && removeUserIds.isEmpty()) {
            log.debug(
                    "[Team] 팀 채널 멤버 변경 없음 | channelId={}",
                    teamChannel.getSendbirdChannelUrl()
            );
            return;
        }

        chatChannelCommandUseCase.updateChannelMembers(
                new UpdateChannelMembersCommand(
                        teamChannel.getSendbirdChannelUrl(),
                        safeList(
                                addUserIds
                        ),
                        safeList(
                                removeUserIds
                        )
                )
        );
    }

    private Set<Long> findCurrentMemberUserIds(
            Long chatChannelId
    ) {
        List<ChatChannelMember> currentMembers =
                chatChannelMemberRepository.findAllByChatChannelIdAndLeftAtIsNull(
                        chatChannelId
                );

        Set<Long> currentMemberUserIds =
                new HashSet<>();

        currentMembers.forEach(member ->
                currentMemberUserIds.add(
                        member.getUserId()
                )
        );

        return currentMemberUserIds;
    }

    private List<Long> safeList(
            List<Long> userIds
    ) {
        if (userIds == null
                || userIds.isEmpty()) {
            return List.of();
        }

        return userIds;
    }
}
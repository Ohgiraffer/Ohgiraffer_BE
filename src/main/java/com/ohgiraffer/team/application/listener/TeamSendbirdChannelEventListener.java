package com.ohgiraffer.team.application.listener;

import com.ohgiraffer.chat.application.command.UpdateChannelMembersCommand;
import com.ohgiraffer.chat.application.usecase.ChatChannelCommandUseCase;
import com.ohgiraffer.chat.domain.model.ChatChannel;
import com.ohgiraffer.chat.domain.model.ChatChannelMember;
import com.ohgiraffer.chat.domain.repository.ChatChannelMemberRepository;
import com.ohgiraffer.chat.domain.repository.ChatChannelRepository;
import com.ohgiraffer.team.application.event.TeamChannelSyncTarget;
import com.ohgiraffer.team.application.event.TeamConfigurationSavedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamSendbirdChannelEventListener {

    private final ChatChannelCommandUseCase chatChannelCommandUseCase;
    private final ChatChannelRepository chatChannelRepository;
    private final ChatChannelMemberRepository chatChannelMemberRepository;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleTeamConfigurationSaved(
            TeamConfigurationSavedEvent event
    ) {
        event.channelSyncTargets()
                .forEach(this::syncTeamChannelSafely);
    }

    private void syncTeamChannelSafely(
            TeamChannelSyncTarget target
    ) {
        try {
            syncTeamChannel(
                    target
            );
        } catch (RuntimeException exception) {
            log.error(
                    "[Team] Sendbird 팀 채널 동기화 실패 | teamId={}, memberUserIds={}",
                    target.teamId(),
                    target.memberUserIds(),
                    exception
            );
        }
    }

    private void syncTeamChannel(
            TeamChannelSyncTarget target
    ) {
        ChatChannel teamChannel =
                findTeamChannel(
                        target.teamId()
                );

        if (teamChannel == null) {
            createTeamChannelIfPossible(
                    target
            );
            return;
        }

        updateTeamChannelMembers(
                teamChannel,
                target.memberUserIds()
        );
    }

    private ChatChannel findTeamChannel(
            Long teamId
    ) {
        List<ChatChannel> teamChannels =
                chatChannelRepository.findAllByTeamId(
                        teamId
                );

        if (teamChannels.size() > 1) {
            log.warn(
                    "[Team] 팀 채널이 여러 개 조회되었습니다. 첫 번째 채널을 사용합니다. | teamId={}, channelCount={}",
                    teamId,
                    teamChannels.size()
            );
        }

        return teamChannels.stream()
                .findFirst()
                .orElse(
                        null
                );
    }

    private void createTeamChannelIfPossible(
            TeamChannelSyncTarget target
    ) {
        if (target.memberUserIds()
                .isEmpty()) {
            log.info(
                    "[Team] 팀 채널 생성 생략 - 멤버 없음 | teamId={}",
                    target.teamId()
            );
            return;
        }

        chatChannelCommandUseCase.createTeamChannel(
                target.teamId(),
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
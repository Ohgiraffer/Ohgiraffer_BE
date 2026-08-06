package com.ohgiraffer.chat.application.usecase;

import com.ohgiraffer.chat.application.command.CreateChannelCommand;
import com.ohgiraffer.chat.application.command.UpdateChannelMembersCommand;
import com.ohgiraffer.chat.application.result.ChatChannelResult;

import java.util.List;

/*
 * comment.
 *  채팅 채널(방) 관련 상태 변경 계약
 */

public interface ChatChannelCommandUseCase {

    ChatChannelResult createChannel(CreateChannelCommand command);

    void updateChannelMembers(UpdateChannelMembersCommand command);

    ChatChannelResult createTeamChannel(Long teamId, List<Long> memberUserIds);

}

package com.ohgiraffer.chat.application.usecase;

import com.ohgiraffer.chat.application.command.MirrorMessageCreatedCommand;
import com.ohgiraffer.chat.application.command.MirrorMessageDeletedCommand;
import com.ohgiraffer.chat.application.command.MirrorMessageUpdatedCommand;

/*
 * comment.
 *  Sendbird 웹훅으로 수신한 메시지/답글 이벤트를 우리 DB(chat_message_mirror)에 반영하는 계약
 */

public interface ChatMessageMirrorCommandUseCase {

    void mirrorCreated(MirrorMessageCreatedCommand command);

    void mirrorUpdated(MirrorMessageUpdatedCommand command);

    void mirrorDeleted(MirrorMessageDeletedCommand command);

}

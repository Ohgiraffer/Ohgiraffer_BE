package com.ohgiraffer.chat.application.usecase;

import com.ohgiraffer.chat.application.command.ProvisionChatUserCommand;
import com.ohgiraffer.chat.application.result.SendbirdUserProvisionResult;

/*
 * comment.
 *  채팅 유저(Sendbird 프로비저닝) 관련 상태 변경 계약
 */

public interface ChatUserCommandUseCase {

    SendbirdUserProvisionResult provisionUser(ProvisionChatUserCommand command);

}

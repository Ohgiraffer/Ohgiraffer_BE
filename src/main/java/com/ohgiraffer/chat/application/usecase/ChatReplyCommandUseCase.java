package com.ohgiraffer.chat.application.usecase;

import com.ohgiraffer.chat.application.command.ReplyToMessageCommand;
import com.ohgiraffer.chat.application.result.SendbirdMessageResult;

/*
 * comment.
 *  스레드 답글 관련 상태 변경 계약
 */

public interface ChatReplyCommandUseCase {

    SendbirdMessageResult reply(ReplyToMessageCommand command);

}

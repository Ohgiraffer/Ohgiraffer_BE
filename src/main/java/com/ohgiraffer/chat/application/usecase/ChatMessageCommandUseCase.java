package com.ohgiraffer.chat.application.usecase;

import com.ohgiraffer.chat.application.command.DeleteMessageCommand;
import com.ohgiraffer.chat.application.command.SendMessageCommand;
import com.ohgiraffer.chat.application.command.UpdateMessageCommand;
import com.ohgiraffer.chat.application.result.SendbirdMessageResult;

/*
 * comment.
 *  일반 메시지 전송/수정/삭제 계약
 *  답글도 update/delete는 동일 메서드로 처리 (같은 chat_message_mirror 레코드)
 */

public interface ChatMessageCommandUseCase {

    SendbirdMessageResult sendMessage(SendMessageCommand command);

    void updateMessage(UpdateMessageCommand command);

    void deleteMessage(DeleteMessageCommand command);

}

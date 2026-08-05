package com.ohgiraffer.chat.application.usecase;

import com.ohgiraffer.chat.application.command.MirrorMessageCreatedCommand;
import com.ohgiraffer.chat.application.command.MirrorMessageDeletedCommand;
import com.ohgiraffer.chat.application.command.MirrorMessageUpdatedCommand;

/*
 * comment.
 *  채팅 메시지 상태 변경(Command) 계약
 *  현재는 웹훅 미러링 3종만 정의, 이후 Sendbird 직접 호출이 필요한
 *  usecase도 이 인터페이스에 메서드로 추가되는 방향으로 확장함
 */

public interface ChatMessageCommandUseCase {

    void mirrorCreated(MirrorMessageCreatedCommand command);

    void mirrorUpdated(MirrorMessageUpdatedCommand command);

    void mirrorDeleted(MirrorMessageDeletedCommand command);


}

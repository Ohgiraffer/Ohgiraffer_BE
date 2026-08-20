package com.ohgiraffer.chat.application.usecase;

import com.ohgiraffer.chat.application.command.ProvisionChatUserCommand;
import com.ohgiraffer.chat.application.result.SendbirdUserProvisionResult;
import com.ohgiraffer.chat.presentation.api.response.SendbirdSessionTokenResponse;

/*
 * comment.
 *  채팅 유저(Sendbird 프로비저닝) 관련 상태 변경 계약
 */

public interface ChatUserCommandUseCase {

    SendbirdUserProvisionResult provisionUser(ProvisionChatUserCommand command);

    // 채팅 진입 시 1회 호출, 신규 유저면 Sendbird 프로비저닝, 기존 유저면 세션 토큰만 재발급
    SendbirdSessionTokenResponse issueSendbirdSessionToken(Long userId);

}

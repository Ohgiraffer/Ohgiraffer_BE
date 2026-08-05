package com.ohgiraffer.chat.application.command;

/*
 * comment.
 *  로그인 후 채팅 진입 시 Sendbird 유저 프로비저닝 커맨드
 *  이미 Sendbird에 등록된 유저면 재사용하고, 없으면 신규 생성함
 */

public record ProvisionChatUserCommand(
        Long userId,
        String nickname,
        String profileUrl
) {
}

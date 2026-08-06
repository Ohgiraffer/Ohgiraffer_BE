package com.ohgiraffer.chat.application.usecase;

import com.ohgiraffer.chat.application.result.SendbirdUserResult;
import com.ohgiraffer.chat.application.result.SendbirdUserStatus;

import java.util.List;

/*
 * comment.
 *  채팅 상대 검색 / 온라인 상태 조회 계약
 *  둘 다 우리 DB를 안 타고 Sendbird API를 직접 조회함
 */

public interface ChatUserQueryUseCase {

    List<SendbirdUserResult> searchUsers(String query);

    SendbirdUserStatus getUserStatus(Long userId);

}

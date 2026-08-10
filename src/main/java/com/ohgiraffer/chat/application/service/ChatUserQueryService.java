package com.ohgiraffer.chat.application.service;

import com.ohgiraffer.chat.application.port.SendbirdApiPort;
import com.ohgiraffer.chat.application.result.SendbirdUserResult;
import com.ohgiraffer.chat.application.result.SendbirdUserStatus;
import com.ohgiraffer.chat.application.usecase.ChatUserQueryUseCase;
import com.ohgiraffer.user.domain.repository.UserRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.user.domain.model.User;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * comment.
 *  ChatUserQueryUseCase 구현체
 *  검색 자체는 우리 DB(users.name) 기준으로 수행 - Sendbird의 nickname_startswith는
 *  "시작 문자 일치"만 지원해서 이름 일부(중간/뒷부분)로는 검색이 안 되는 제약이 있었음
 *  Sendbird는 검색된 유저의 온라인 상태 확인용으로만 사용
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatUserQueryService implements ChatUserQueryUseCase {

    private final SendbirdApiPort sendbirdApiPort;
    private final UserRepository userRepository;

    // 채팅 상대 검색 - 우리 DB에서 이름 부분검색 후, 검색된 유저들의 온라인 상태만 Sendbird에서 조회
    @Override
    public List<SendbirdUserResult> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return List.of(); // 빈 검색어는 전체 조회가 아니라 빈 결과로 처리
        }

        List<User> matchedUsers = userRepository.findByNameContaining(query);

        return matchedUsers.stream()
                .map(this::toSendbirdUserResult)
                .toList();
    }

    // User(로컬 DB) + Sendbird 온라인 상태를 조합해서 결과 생성
    // Sendbird에 프로비저닝 안 된 유저(로그인 이력 없음)는 조회 실패할 수 있어 offline으로 기본 처리
    private SendbirdUserResult toSendbirdUserResult(User user) {
        boolean isOnline = fetchOnlineStatus(user.getId());

        return new SendbirdUserResult(
                user.getId(),
                user.getName(),
                user.getProfileImg(),
                isOnline,
                user.getRole().name()
        );
    }

    // Sendbird 온라인 상태 조회 - 실패(미프로비저닝 등)하면 offline으로 기본 처리, 검색 자체를 막지 않음
    private boolean fetchOnlineStatus(Long userId) {
        try {
            SendbirdUserStatus status = sendbirdApiPort.getUserStatus(userId);
            return status.isOnline();
        } catch (BusinessException e) {
            log.warn("[Chat] 온라인 상태 조회 실패 - offline으로 처리 | userId={}", userId);
            return false;
        }
    }

    // 온라인 상태 조회 - Sendbird 접속정보 그대로 위임
    @Override
    public SendbirdUserStatus getUserStatus(Long userId) {
        return sendbirdApiPort.getUserStatus(userId);
    }

}

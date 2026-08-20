package com.ohgiraffer.chat.application.service;

import com.ohgiraffer.chat.application.command.ProvisionChatUserCommand;
import com.ohgiraffer.chat.application.port.SendbirdApiPort;
import com.ohgiraffer.chat.application.result.SendbirdUserProvisionResult;
import com.ohgiraffer.chat.application.usecase.ChatUserCommandUseCase;
import com.ohgiraffer.chat.infrastructure.sendbird.SendbirdProperties;
import com.ohgiraffer.chat.presentation.api.response.SendbirdSessionTokenResponse;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.s3.S3UrlResolver;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/*
 * comment.
 *  ChatUserCommandUseCase 구현체
 *  실제 프로비저닝 로직(생성 vs 재사용 판단)은 SendbirdApiAdapter가 담당,
 *  이 서비스는 usecase 계약과 port 호출만 연결함
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatUserCommandService implements ChatUserCommandUseCase {

    // Sendbird Platform API 호출용 아웃바운드 포트, 이미 존재하는 SendbirdApiAdapter가 구현하는 것으로 가정
    private final SendbirdApiPort sendbirdApiPort;
    // 세션 토큰 발급 시 최신 name/profileImg를 넘기기 위해 필요
    private final UserRepository userRepository;
    // appId(공개값) 응답에 담기 위해 필요
    private final SendbirdProperties sendbirdProperties;
    // profileImg(S3 key)를 Sendbird에 넘길 수 있는 실제 접근 URL로 변환
    private final S3UrlResolver s3UrlResolver;

    // 로그인 후 채팅 진입 시 Sendbird 유저 프로비저닝 - 이미 등록된 유저면 SendbirdApiAdapter가 알아서 토큰만 재발급
    @Override
    public SendbirdUserProvisionResult provisionUser(ProvisionChatUserCommand command) {
        SendbirdUserProvisionResult result = sendbirdApiPort.provisionUser(
                command.userId(),
                command.name(),
                command.profileUrl()
        );

        log.info("[Chat] Sendbird 유저 프로비저닝 완료 | userId={}, name={}",
                result.userId(), result.name());

        return result;
    }

    // 채팅 진입 시 1회 호출 - provisionUser와 동일 로직 재사용(신규면 생성, 기존이면 토큰만 재발급)
    // Sendbird access token은 콘솔에서 별도 TTL을 설정하지 않는 한 기본적으로 만료되지 않아 expiresAt은 null로 내려감
    // (TTL 정책을 도입하면 그때 실제 만료시각 계산 로직 추가 필요)
    @Override
    public SendbirdSessionTokenResponse issueSendbirdSessionToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // user.getProfileImg()는 S3 key 원본이라 Sendbird에 그대로 넘기면 접근 불가능한 값이 등록됨
        // -> presigned URL로 변환 후 전달 (채팅 진입마다 호출되므로 여기서 누락되면 프사가 계속 깨진 값으로 덮어써짐)
        String profileUrl = resolveProfileImgUrl(user.getProfileImg());

        SendbirdUserProvisionResult result = sendbirdApiPort.provisionUser(
                userId, user.getName(), profileUrl
        );

        log.info("[Chat] Sendbird 세션 토큰 발급 완료 | userId={}", userId);

        return new SendbirdSessionTokenResponse(
                String.valueOf(result.userId()),
                result.accessToken(),
                sendbirdProperties.appId(),
                null
        );
    }

    private String resolveProfileImgUrl(String profileImgKey) {
        if (profileImgKey == null || profileImgKey.isBlank()) {
            return null;
        }
        return s3UrlResolver.resolve(profileImgKey);
    }

}
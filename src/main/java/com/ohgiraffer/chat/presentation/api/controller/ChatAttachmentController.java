package com.ohgiraffer.chat.presentation.api.controller;

import com.ohgiraffer.chat.domain.model.ChatChannel;
import com.ohgiraffer.chat.domain.repository.ChatChannelMemberRepository;
import com.ohgiraffer.chat.domain.repository.ChatChannelRepository;
import com.ohgiraffer.chat.presentation.api.response.ChatAttachmentResponse;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.s3.S3FileHandler;
import com.ohgiraffer.global.s3.S3KeyGenerator;
import com.ohgiraffer.global.s3.S3UrlResolver;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/*
 * comment.
 *  채팅 첨부파일 업로드 엔드포인트
 *  업로드 전 요청자가 해당 채널의 활성 멤버인지 검증 - 아니면 아무 채널에나 파일을 올릴 수 있는 IDOR 취약점이 됨
 */


@RestController
@RequestMapping("/chat/channels/{channelId}/attachments")
@RequiredArgsConstructor
public class ChatAttachmentController {

    // S3 업로드 실행
    private final S3FileHandler s3FileHandler;
    // 업로드된 key로 접근 URL 생성
    private final S3UrlResolver s3UrlResolver;
    // 채널 조회 + 멤버십 검증용
    private final ChatChannelRepository chatChannelRepository;
    private final ChatChannelMemberRepository chatChannelMemberRepository;

    @PostMapping
    public ResponseEntity<ChatAttachmentResponse> upload(
            // 인증만 검증, 별도 소유권 체크는 불필요(첨부파일은 채널 멤버 누구나 업로드 가능)
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String channelId,
            @RequestPart("file") MultipartFile file
    ) {

        // channelId(sendbird url) 기준으로 우리 DB의 채널 레코드 조회
        ChatChannel channel = chatChannelRepository.findBySendbirdChannelUrl(channelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND));

        // IDOR 방지 - 채널 멤버가 아니면 존재 자체를 숨기고 404로 응답
        if (!chatChannelMemberRepository.existsActiveMembership(channel.getId(), principal.getId())) {
            throw new BusinessException(ErrorCode.CHAT_CHANNEL_NOT_FOUND);
        }

        String key = S3KeyGenerator.chatAttachmentKey(channelId, file.getOriginalFilename());
        s3FileHandler.upload(file, key);

        // chatAttachments prefix는 public이므로 고정 URL 사용
        String url = s3UrlResolver.resolvePublicUrl(key);
        return ResponseEntity.ok(new ChatAttachmentResponse(url));
    }

}

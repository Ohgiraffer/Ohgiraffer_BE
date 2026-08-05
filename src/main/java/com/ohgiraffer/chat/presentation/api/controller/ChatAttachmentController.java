package com.ohgiraffer.chat.presentation.api.controller;

import com.ohgiraffer.chat.presentation.api.response.ChatAttachmentResponse;
import com.ohgiraffer.global.s3.S3FileHandler;
import com.ohgiraffer.global.s3.S3KeyGenerator;
import com.ohgiraffer.global.s3.S3UrlResolver;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/chat/channels/{channelId}/attachments")
@RequiredArgsConstructor
public class ChatAttachmentController {

    // S3 업로드 실행
    private final S3FileHandler s3FileHandler;
    // 업로드된 key로 접근 URL 생성
    private final S3UrlResolver s3UrlResolver;

    @PostMapping
    public ResponseEntity<ChatAttachmentResponse> upload(
            // 인증만 검증, 별도 소유권 체크는 불필요(첨부파일은 채널 멤버 누구나 업로드 가능)
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String channelId,
            @RequestPart("file") MultipartFile file
    ) {
        String key = S3KeyGenerator.chatAttachmentKey(channelId, file.getOriginalFilename());
        s3FileHandler.upload(file, key);

        // chatAttachments prefix는 public이므로 고정 URL 사용
        String url = s3UrlResolver.resolvePublicUrl(key);
        return ResponseEntity.ok(new ChatAttachmentResponse(url));
    }

}

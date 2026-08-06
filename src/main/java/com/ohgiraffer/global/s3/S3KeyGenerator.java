package com.ohgiraffer.global.s3;

import java.util.UUID;

public class S3KeyGenerator {

    private S3KeyGenerator() {}

    public static String profileImageKey(Long userId) {
        return "profileImg/" + userId;
    }

    // 채팅 첨부파일 - 채널별로 묶고, 매번 UUID를 붙여 파일명 충돌/덮어쓰기 방지
    public static String chatAttachmentKey(String channelId, String originalFileName) {
        String safeFileName = originalFileName == null ? "file" : originalFileName;
        return "chatAttachments/" + channelId + "/" + UUID.randomUUID() + "_" + safeFileName;
    }

}

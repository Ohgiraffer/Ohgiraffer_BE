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

    public static String submissionFileKey(
            Long submissionBoxId,
            Long submittedBy,
            String originalFileName
    ) {
        String extension = extractSafeExtension(originalFileName);

        return "submissions/"
                + submissionBoxId
                + "/"
                + submittedBy
                + "/"
                + UUID.randomUUID()
                + extension;
    }

    private static String extractSafeExtension(
            String originalFileName
    ) {
        if (originalFileName == null
                || originalFileName.isBlank()) {
            return "";
        }

        int dotIndex = originalFileName.lastIndexOf('.');

        if (dotIndex < 0
                || dotIndex == originalFileName.length() - 1) {
            return "";
        }

        String extension = originalFileName
                .substring(dotIndex + 1)
                .toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");

        if (extension.isBlank()) {
            return "";
        }

        // 비정상적으로 긴 확장자로 S3 키가 커지는 것을 방지
        if (extension.length() > 20) {
            extension = extension.substring(0, 20);
        }

        return "." + extension;
    }

}

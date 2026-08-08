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

    // 공지 본문에 삽입하는 이미지 - 글을 쓰는 중에 올라와 아직 공지 번호가 없다.
    // 그래서 공지별로 묶지 못하고 한 prefix 아래에 UUID로만 구분한다.
    public static String noticeImageKey(String originalFileName) {
        return "noticeImages/"
                + UUID.randomUUID()
                + extractSafeExtension(originalFileName);
    }

    // 공지 첨부파일 - 등록 화면에서 파일을 고르는 순간 올라오므로 아직 공지 번호가 없다.
    // 그래서 공지별로 묶지 못하고 본문 이미지와 마찬가지로 UUID 로만 구분한다.
    public static String noticeAttachmentKey(String originalFileName) {
        return "noticeAttachments/"
                + UUID.randomUUID()
                + extractSafeExtension(originalFileName);
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

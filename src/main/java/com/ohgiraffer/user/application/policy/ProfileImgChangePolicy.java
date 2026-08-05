package com.ohgiraffer.user.application.policy;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

public class ProfileImgChangePolicy {

    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024; // 50MB

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png"
    );

    private ProfileImgChangePolicy() {}

    public static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.MISSING_PROFILE_IMAGE);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.PROFILE_IMAGE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE_TYPE);
        }

        validateFileSignature(file);
    }

    private static void validateFileSignature(MultipartFile file) {
        byte[] header = new byte[8];

        try {
            int read = file.getInputStream().read(header);
            if (read < 4 || !isValidImageSignature(header)) {
                throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE_TYPE);
            }
        } catch (IOException e) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "프로필 이미지 파일을 읽을 수 없습니다."
            );
        }
    }

    private static boolean isValidImageSignature(byte[] header) {
        // PNG: 89 50 4E 47
        boolean isPng = (header[0] & 0xFF) == 0x89
                && header[1] == 0x50
                && header[2] == 0x4E
                && header[3] == 0x47;

        // JPEG: FF D8 FF
        boolean isJpeg = (header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF;

        return isPng || isJpeg;
    }
}
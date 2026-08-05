package com.ohgiraffer.approval.application.usecase;

import com.ohgiraffer.approval.domain.model.signature.UserSignature;

import java.time.LocalDateTime;
import java.util.Base64;

public record SignatureResult(
        Long signatureId,
        Long userId,
        String signatureImage,
        String originalFileName,
        Long fileSizeBytes,
        String fileType,
        boolean active,
        LocalDateTime updatedAt
) {

    public static SignatureResult from(
            UserSignature userSignature
    ) {
        return new SignatureResult(
                userSignature.getId(),
                userSignature.getUserId(),
                toDataUri(
                        userSignature.getSignatureImage(),
                        userSignature.getFileType()
                ),
                userSignature.getOriginalFileName(),
                userSignature.getFileSizeBytes(),
                userSignature.getFileType(),
                userSignature.isActive(),
                userSignature.getUpdatedAt()
        );
    }

    private static String toDataUri(
            byte[] image,
            String fileType
    ) {
        if (image == null || image.length == 0) {
            return null;
        }

        String base64 =
                Base64
                        .getEncoder()
                        .encodeToString(
                                image
                        );

        return "data:" + fileType + ";base64," + base64;
    }
}
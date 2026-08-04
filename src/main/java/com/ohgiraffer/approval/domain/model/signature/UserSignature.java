package com.ohgiraffer.approval.domain.model.signature;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSignature {

    private final Long id;
    private final Long userId;
    private byte[] signatureImage;
    private String originalFileName;
    private Long fileSizeBytes;
    private String fileType;
    private boolean active;
    private LocalDateTime updatedAt;

    public static UserSignature create(
            Long userId,
            byte[] signatureImage,
            String originalFileName,
            Long fileSizeBytes,
            String fileType,
            LocalDateTime updatedAt
    ) {
        UserSignature userSignature =
                new UserSignature(
                        null,
                        userId
                );

        userSignature.replaceImage(
                signatureImage,
                originalFileName,
                fileSizeBytes,
                fileType,
                updatedAt
        );

        return userSignature;
    }

    public static UserSignature restore(
            Long id,
            Long userId,
            byte[] signatureImage,
            String originalFileName,
            Long fileSizeBytes,
            String fileType,
            boolean active,
            LocalDateTime updatedAt
    ) {
        UserSignature userSignature =
                new UserSignature(
                        id,
                        userId
                );

        userSignature.signatureImage = copyBytes(signatureImage);
        userSignature.originalFileName = originalFileName;
        userSignature.fileSizeBytes = fileSizeBytes;
        userSignature.fileType = fileType;
        userSignature.active = active;
        userSignature.updatedAt = updatedAt;

        return userSignature;
    }

    public void replaceImage(
            byte[] signatureImage,
            String originalFileName,
            Long fileSizeBytes,
            String fileType,
            LocalDateTime updatedAt
    ) {
        this.signatureImage = copyBytes(signatureImage);
        this.originalFileName = originalFileName;
        this.fileSizeBytes = fileSizeBytes;
        this.fileType = fileType;
        this.active = true;
        this.updatedAt = updatedAt;
    }

    public void deactivate(
            LocalDateTime updatedAt
    ) {
        this.active = false;
        this.updatedAt = updatedAt;
    }

    public boolean isOwnedBy(
            Long userId
    ) {
        return this.userId.equals(userId);
    }

    public byte[] getSignatureImage() {
        return copyBytes(signatureImage);
    }

    private static byte[] copyBytes(
            byte[] source
    ) {
        if (source == null) {
            return null;
        }

        return Arrays.copyOf(
                source,
                source.length
        );
    }
}
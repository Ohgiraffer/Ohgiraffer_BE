package com.ohgiraffer.approval.infrastructure.persistence;

import com.ohgiraffer.approval.domain.model.UserSignature;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;

@Entity
@Table(name = "user_signature")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSignatureJpaEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "signature_id")
    private Long id;

    @Lob
    @Column(
            name = "signature_image",
            nullable = false,
            columnDefinition = "LONGBLOB"
    )
    private byte[] signatureImage;

    @Column(
            name = "original_file_name",
            nullable = false,
            length = 255
    )
    private String originalFileName;

    @Column(
            name = "file_size_bytes",
            nullable = false
    )
    private Long fileSizeBytes;

    @Column(
            name = "file_type",
            nullable = false,
            length = 50
    )
    private String fileType;

    @Column(
            name = "is_active",
            nullable = false
    )
    private boolean active;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @Column(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private Long userId;

    private UserSignatureJpaEntity(
            Long id,
            byte[] signatureImage,
            String originalFileName,
            Long fileSizeBytes,
            String fileType,
            boolean active,
            LocalDateTime updatedAt,
            Long userId
    ) {
        this.id = id;
        this.signatureImage = copyBytes(
                signatureImage
        );
        this.originalFileName = originalFileName;
        this.fileSizeBytes = fileSizeBytes;
        this.fileType = fileType;
        this.active = active;
        this.updatedAt = updatedAt;
        this.userId = userId;
    }

    public static UserSignatureJpaEntity from(
            UserSignature userSignature
    ) {
        return new UserSignatureJpaEntity(
                userSignature.getId(),
                userSignature.getSignatureImage(),
                userSignature.getOriginalFileName(),
                userSignature.getFileSizeBytes(),
                userSignature.getFileType(),
                userSignature.isActive(),
                userSignature.getUpdatedAt(),
                userSignature.getUserId()
        );
    }

    public UserSignature toDomain() {
        return UserSignature.restore(
                id,
                userId,
                signatureImage,
                originalFileName,
                fileSizeBytes,
                fileType,
                active,
                updatedAt
        );
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
package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.command.RegisterSignatureCommand;
import com.ohgiraffer.approval.application.usecase.RegisterSignatureUseCase;
import com.ohgiraffer.approval.application.usecase.SignatureResult;
import com.ohgiraffer.approval.domain.model.signature.UserSignature;
import com.ohgiraffer.approval.domain.repository.UserSignatureRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

@Service
public class RegisterSignatureService implements RegisterSignatureUseCase {

    private static final long MAX_FILE_SIZE_BYTES = 1024L * 1024L;

    private static final Set<String> ALLOWED_FILE_TYPES =
            Set.of("image/png", "image/jpeg");

    private final UserSignatureRepository userSignatureRepository;
    private final Clock clock;

    public RegisterSignatureService(
            UserSignatureRepository userSignatureRepository,
            Clock clock
    ) {
        this.userSignatureRepository = userSignatureRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public SignatureResult register(RegisterSignatureCommand command) {
        validateBasicFileInfo(command);

        String detectedFileType =
                detectAndValidateImageType(command.signatureImage());

        validateDeclaredFileType(
                command.fileType(),
                detectedFileType
        );

        validateNotAlreadyRegistered(command.userId());

        LocalDateTime now = LocalDateTime.now(clock);

        UserSignature userSignature =
                userSignatureRepository
                        .findByUserId(command.userId())
                        .map(existingSignature -> {
                            existingSignature.replaceImage(
                                    command.signatureImage(),
                                    command.originalFileName(),
                                    command.fileSizeBytes(),
                                    detectedFileType,
                                    now
                            );
                            return existingSignature;
                        })
                        .orElseGet(() ->
                                UserSignature.create(
                                        command.userId(),
                                        command.signatureImage(),
                                        command.originalFileName(),
                                        command.fileSizeBytes(),
                                        detectedFileType,
                                        now
                                )
                        );

        UserSignature savedSignature =
                userSignatureRepository.save(userSignature);

        return SignatureResult.from(savedSignature);
    }

    private void validateNotAlreadyRegistered(Long userId) {
        if (userSignatureRepository.existsActiveByUserId(userId)) {
            throw new BusinessException(
                    ErrorCode.SIGNATURE_ALREADY_EXISTS
            );
        }
    }

    private void validateBasicFileInfo(RegisterSignatureCommand command) {
        if (command.signatureImage() == null
                || command.signatureImage().length == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "전자서명 이미지 파일은 필수입니다."
            );
        }

        if (command.fileSizeBytes() == null
                || command.fileSizeBytes() <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "전자서명 이미지 파일 크기가 올바르지 않습니다."
            );
        }

        if (command.fileSizeBytes() > MAX_FILE_SIZE_BYTES) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "전자서명 이미지 파일은 1MB 이하만 업로드할 수 있습니다."
            );
        }

        if (command.originalFileName() == null
                || command.originalFileName().isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "전자서명 이미지 파일명이 올바르지 않습니다."
            );
        }
    }

    private String detectAndValidateImageType(byte[] imageBytes) {
        try (ImageInputStream imageInputStream =
                     ImageIO.createImageInputStream(
                             new ByteArrayInputStream(imageBytes)
                     )) {
            if (imageInputStream == null) {
                throw invalidImageTypeException();
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(imageInputStream);

            if (!readers.hasNext()) {
                throw invalidImageTypeException();
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(imageInputStream, true, true);

                /*
                 * 실제 이미지 메타데이터를 읽어 디코딩 가능한 이미지인지 검증합니다.
                 */
                reader.getWidth(0);
                reader.getHeight(0);

                String formatName =
                        reader.getFormatName()
                                .toLowerCase(Locale.ROOT);

                if ("png".equals(formatName)) {
                    return "image/png";
                }

                if ("jpeg".equals(formatName)
                        || "jpg".equals(formatName)) {
                    return "image/jpeg";
                }

                throw invalidImageTypeException();

            } finally {
                reader.dispose();
            }

        } catch (IOException exception) {
            throw invalidImageTypeException();
        }
    }

    private void validateDeclaredFileType(
            String declaredFileType,
            String detectedFileType
    ) {
        if (declaredFileType == null
                || declaredFileType.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "전자서명 이미지 파일 형식을 확인할 수 없습니다."
            );
        }

        if (!ALLOWED_FILE_TYPES.contains(declaredFileType)
                || !declaredFileType.equals(detectedFileType)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "전자서명 이미지 파일 형식과 실제 이미지 형식이 일치하지 않습니다."
            );
        }
    }

    private BusinessException invalidImageTypeException() {
        return new BusinessException(
                ErrorCode.INVALID_INPUT_VALUE,
                "전자서명 이미지는 PNG 또는 JPEG 형식만 업로드할 수 있습니다."
        );
    }
}
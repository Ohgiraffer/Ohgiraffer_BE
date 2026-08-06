package com.ohgiraffer.approval.presentation.api;

import com.ohgiraffer.approval.application.command.RegisterSignatureCommand;
import com.ohgiraffer.approval.application.usecase.DeleteMySignatureUseCase;
import com.ohgiraffer.approval.application.usecase.GetMySignatureUseCase;
import com.ohgiraffer.approval.application.usecase.RegisterSignatureUseCase;
import com.ohgiraffer.approval.application.usecase.SignatureResult;
import com.ohgiraffer.approval.presentation.api.response.SignatureResponse;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/signatures")
public class SignatureController {

    private final RegisterSignatureUseCase registerSignatureUseCase;
    private final GetMySignatureUseCase getMySignatureUseCase;
    private final DeleteMySignatureUseCase deleteMySignatureUseCase;

    public SignatureController(
            RegisterSignatureUseCase registerSignatureUseCase,
            GetMySignatureUseCase getMySignatureUseCase,
            DeleteMySignatureUseCase deleteMySignatureUseCase
    ) {
        this.registerSignatureUseCase = registerSignatureUseCase;
        this.getMySignatureUseCase = getMySignatureUseCase;
        this.deleteMySignatureUseCase = deleteMySignatureUseCase;
    }

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<SignatureResponse> registerSignature(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        RegisterSignatureCommand command =
                new RegisterSignatureCommand(
                        principal.getId(),
                        getBytes(
                                file
                        ),
                        file.getOriginalFilename(),
                        file.getSize(),
                        file.getContentType()
                );

        SignatureResult result =
                registerSignatureUseCase.register(
                        command
                );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        SignatureResponse.from(
                                result
                        )
                );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<SignatureResponse> getMySignature(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        SignatureResult result =
                getMySignatureUseCase.getMySignature(
                        principal.getId()
                );

        return ResponseEntity
                .ok(
                        SignatureResponse.from(
                                result
                        )
                );
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<Void> deleteMySignature(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        deleteMySignatureUseCase.deleteMySignature(
                principal.getId()
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    private byte[] getBytes(
            MultipartFile file
    ) {
        try {
            return file.getBytes();

        } catch (IOException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "전자서명 이미지 파일을 읽을 수 없습니다."
            );
        }
    }
}
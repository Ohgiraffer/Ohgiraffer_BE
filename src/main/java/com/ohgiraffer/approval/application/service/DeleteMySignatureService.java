package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.usecase.DeleteMySignatureUseCase;
import com.ohgiraffer.approval.domain.model.signature.UserSignature;
import com.ohgiraffer.approval.domain.repository.UserSignatureRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class DeleteMySignatureService
        implements DeleteMySignatureUseCase {

    private final UserSignatureRepository userSignatureRepository;
    private final Clock clock;

    public DeleteMySignatureService(
            UserSignatureRepository userSignatureRepository,
            Clock clock
    ) {
        this.userSignatureRepository = userSignatureRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void deleteMySignature(
            Long userId
    ) {
        UserSignature userSignature =
                userSignatureRepository
                        .findActiveByUserId(
                                userId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.SIGNATURE_NOT_FOUND
                                )
                        );

        userSignature.deactivate(
                LocalDateTime.now(
                        clock
                )
        );

        userSignatureRepository.save(
                userSignature
        );
    }
}
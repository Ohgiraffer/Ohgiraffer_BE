package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.usecase.GetMySignatureUseCase;
import com.ohgiraffer.approval.application.usecase.SignatureResult;
import com.ohgiraffer.approval.domain.model.signature.UserSignature;
import com.ohgiraffer.approval.domain.repository.UserSignatureRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetMySignatureService
        implements GetMySignatureUseCase {

    private final UserSignatureRepository userSignatureRepository;

    public GetMySignatureService(
            UserSignatureRepository userSignatureRepository
    ) {
        this.userSignatureRepository = userSignatureRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SignatureResult getMySignature(
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

        return SignatureResult.from(
                userSignature
        );
    }
}
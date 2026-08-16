package com.ohgiraffer.auditlog.application.usecase;

import com.ohgiraffer.auditlog.domain.dto.AuditChainVerificationResult;

import java.util.List;

public interface VerifyAuditChainUsecase {

    AuditChainVerificationResult verify(String domainName);

    List<AuditChainVerificationResult> verifyAll();
}
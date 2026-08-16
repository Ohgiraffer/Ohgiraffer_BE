package com.ohgiraffer.auditlog.application.service;

import com.ohgiraffer.auditlog.application.usecase.VerifyAuditChainUsecase;
import com.ohgiraffer.auditlog.domain.dto.AuditChainVerificationResult;
import com.ohgiraffer.auditlog.domain.model.AuditLog;
import com.ohgiraffer.auditlog.domain.repository.AuditLogRepository;
import com.ohgiraffer.auditlog.infrastructure.crypto.AuditHashSigner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditChainVerificationService implements VerifyAuditChainUsecase {

    private static final String GENESIS_HASH = "0";

    private final AuditLogRepository auditLogRepository;
    private final AuditHashSigner auditHashSigner;

    @Override
    @Transactional(readOnly = true)
    public AuditChainVerificationResult verify(String domainName) {
        List<AuditLog> logs = auditLogRepository.findAllByDomainNameOrderByIdAsc(domainName);

        String expectedPreviousHash = GENESIS_HASH;
        int checked = 0;

        for (AuditLog log : logs) {
            if (!expectedPreviousHash.equals(log.getPreviousHash())) {
                return AuditChainVerificationResult.broken(domainName, log.getId(), checked);
            }

            String recomputedHash = auditHashSigner.sign(log.buildRawPayload());
            if (!recomputedHash.equals(log.getHash())) {
                return AuditChainVerificationResult.broken(domainName, log.getId(), checked);
            }

            expectedPreviousHash = log.getHash();
            checked++;
        }

        return AuditChainVerificationResult.valid(domainName, checked);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditChainVerificationResult> verifyAll() {
        return auditLogRepository.findAllDistinctDomainNames().stream()
                .map(this::verify)
                .toList();
    }
}
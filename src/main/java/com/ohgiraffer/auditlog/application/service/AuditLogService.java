package com.ohgiraffer.auditlog.application.service;

import com.ohgiraffer.auditlog.application.command.RecordAuditLogCommand;
import com.ohgiraffer.auditlog.application.usecase.RecordAuditLogUsecase;
import com.ohgiraffer.auditlog.domain.model.AuditLog;
import com.ohgiraffer.auditlog.domain.repository.AuditLogRepository;
import com.ohgiraffer.auditlog.infrastructure.crypto.AuditHashSigner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService implements RecordAuditLogUsecase {

    private static final String GENESIS_HASH = "0";

    private final AuditLogRepository auditLogRepository;
    private final AuditHashSigner auditHashSigner;

    @Override
    @Transactional
    public void record(RecordAuditLogCommand command) {
        String previousHash = auditLogRepository.findLatestHashByDomainName(command.domainName())
                .orElse(GENESIS_HASH);
        LocalDateTime occurredAt = LocalDateTime.now();

        AuditLog draft = AuditLog.create(
                command.domainName(),
                command.eventType(),
                command.actorId(),
                command.targetId(),
                command.beforeValue(),
                command.afterValue(),
                previousHash,
                null,
                occurredAt
        );

        String hash = auditHashSigner.sign(draft.buildRawPayload());

        AuditLog auditLog = AuditLog.create(
                command.domainName(),
                command.eventType(),
                command.actorId(),
                command.targetId(),
                command.beforeValue(),
                command.afterValue(),
                previousHash,
                hash,
                occurredAt
        );

        auditLogRepository.save(auditLog);
    }
}
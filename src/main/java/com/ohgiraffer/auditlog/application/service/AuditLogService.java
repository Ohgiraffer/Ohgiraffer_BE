package com.ohgiraffer.auditlog.application.service;

import com.ohgiraffer.auditlog.application.command.RecordAuditLogCommand;
import com.ohgiraffer.auditlog.application.usecase.RecordAuditLogUsecase;
import com.ohgiraffer.auditlog.domain.model.AuditLog;
import com.ohgiraffer.auditlog.domain.repository.AuditLogRepository;
import com.ohgiraffer.auditlog.infrastructure.crypto.AuditHashSigner;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuditLogService implements RecordAuditLogUsecase {

    private static final String GENESIS_HASH = "0";
    private static final long LOCK_WAIT_SECONDS = 3L;
    private static final long LOCK_LEASE_SECONDS = 5L;

    private final AuditLogRepository auditLogRepository;
    private final AuditHashSigner auditHashSigner;
    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate; // REQUIRES_NEW로 별도 빈 등록

    @Override
    public void record(RecordAuditLogCommand command) {
        RLock lock = redissonClient.getLock("audit_log_lock:" + command.domainName());

        boolean acquired;
        try {
            acquired = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.LOCK_WAIT_INTERRUPTED);
        }

        if (!acquired) {
            throw new BusinessException(ErrorCode.LOCK_ACQUISITION_FAILED);
        }

        try {
            // executeWithoutResult가 리턴하는 시점 == 이 트랜잭션이 이미 커밋 완료된 시점
            transactionTemplate.executeWithoutResult(status -> {
                String previousHash = auditLogRepository.findLatestHashByDomainName(command.domainName())
                        .orElse(GENESIS_HASH);
                LocalDateTime occurredAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

                AuditLog draft = AuditLog.create(
                        command.domainName(), command.eventType(), command.actorId(),
                        command.targetId(), command.beforeValue(), command.afterValue(),
                        previousHash, null, occurredAt
                );

                String hash = auditHashSigner.sign(draft.buildRawPayload());

                AuditLog auditLog = AuditLog.create(
                        command.domainName(), command.eventType(), command.actorId(),
                        command.targetId(), command.beforeValue(), command.afterValue(),
                        previousHash, hash, occurredAt
                );

                auditLogRepository.save(auditLog);
            });
        } finally {
            // 여기 도달했을 땐 위 트랜잭션이 이미 커밋된 뒤이므로 안전하게 해제
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
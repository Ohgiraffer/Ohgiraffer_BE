package com.ohgiraffer.auditlog.infrastructure.scheduler;

import com.ohgiraffer.auditlog.application.usecase.VerifyAuditChainUsecase;
import com.ohgiraffer.auditlog.domain.dto.AuditChainVerificationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditChainVerificationScheduler {

    private final VerifyAuditChainUsecase verifyAuditChainUsecase;

    @Scheduled(cron = "0 0 4 * * *")
    public void verifyAllChains() {
        for (AuditChainVerificationResult result : verifyAuditChainUsecase.verifyAll()) {
            if (!result.valid()) {
                log.error("[AuditChain] 위변조 감지 - domain: {}, brokenAtLogId: {}, checked: {}",
                        result.domainName(), result.brokenAtLogId(), result.totalChecked());
            } else {
                log.info("[AuditChain] 검증 정상 - domain: {}, checked: {}",
                        result.domainName(), result.totalChecked());
            }
        }
    }
}
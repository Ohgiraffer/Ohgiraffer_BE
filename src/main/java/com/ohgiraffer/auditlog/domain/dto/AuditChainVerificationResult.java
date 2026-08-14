package com.ohgiraffer.auditlog.domain.dto;

public record AuditChainVerificationResult(
        String domainName,
        boolean valid,
        Long brokenAtLogId,
        int totalChecked
) {
    public static AuditChainVerificationResult valid(String domainName, int totalChecked) {
        return new AuditChainVerificationResult(domainName, true, null, totalChecked);
    }

    public static AuditChainVerificationResult broken(String domainName, Long brokenAtLogId, int totalChecked) {
        return new AuditChainVerificationResult(domainName, false, brokenAtLogId, totalChecked);
    }
}
package com.ohgiraffer.auditlog.domain.repository;

import com.ohgiraffer.auditlog.domain.model.AuditLog;

import java.util.List;
import java.util.Optional;

public interface AuditLogRepository {

    AuditLog save(AuditLog auditLog);

    Optional<String> findLatestHashByDomainName(String domainName);

    List<AuditLog> findAllByDomainNameOrderByIdAsc(String domainName);

    List<String> findAllDistinctDomainNames();
}
package com.ohgiraffer.auditlog.infrastructure.persistence;

import com.ohgiraffer.auditlog.domain.model.AuditLog;
import com.ohgiraffer.auditlog.domain.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements AuditLogRepository {

    private final SpringDataAuditLogRepository springDataAuditLogRepository;

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogJpaEntity saved = springDataAuditLogRepository.save(AuditLogJpaEntity.from(auditLog));
        return saved.toDomain();
    }

    @Override
    public Optional<String> findLatestHashByDomainName(String domainName) {
        return springDataAuditLogRepository.findLatestHashByDomainName(domainName);
    }

    @Override
    public List<AuditLog> findAllByDomainNameOrderByIdAsc(String domainName) {
        return springDataAuditLogRepository.findAllByDomainNameOrderByIdAsc(domainName).stream()
                .map(AuditLogJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<String> findAllDistinctDomainNames() {
        return springDataAuditLogRepository.findAllDistinctDomainNames();
    }
}
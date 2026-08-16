package com.ohgiraffer.auditlog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataAuditLogRepository extends JpaRepository<AuditLogJpaEntity, Long> {

    @Query("SELECT a.hash FROM AuditLogJpaEntity a " +
            "WHERE a.domainName = :domainName " +
            "ORDER BY a.id DESC LIMIT 1")
    Optional<String> findLatestHashByDomainName(@Param("domainName") String domainName);

    List<AuditLogJpaEntity> findAllByDomainNameOrderByIdAsc(String domainName);

    @Query("SELECT DISTINCT a.domainName FROM AuditLogJpaEntity a")
    List<String> findAllDistinctDomainNames();
}
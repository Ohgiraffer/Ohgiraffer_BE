package com.ohgiraffer.notice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNoticeCategoryRepository
        extends JpaRepository<NoticeCategoryJpaEntity, Long> {
}

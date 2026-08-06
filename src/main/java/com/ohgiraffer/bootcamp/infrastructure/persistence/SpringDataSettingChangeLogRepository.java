package com.ohgiraffer.bootcamp.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataSettingChangeLogRepository extends JpaRepository<SettingChangeLogJpaEntity, Long> {
    List<SettingChangeLogJpaEntity> findAllByBootcampIdOrderByChangedAtDesc(Long bootcampId);

}

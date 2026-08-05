package com.ohgiraffer.bootcamp.infrastructure.persistence;

import com.ohgiraffer.bootcamp.domain.model.SettingChangeLog;
import com.ohgiraffer.bootcamp.domain.repository.SettingChangeLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SettingChangeLogRepositoryAdapter implements SettingChangeLogRepository {

    private final SpringDataSettingChangeLogRepository springDataSettingChangeLogRepository;

    @Override
    public List<SettingChangeLog> saveAll(List<SettingChangeLog> logs) {
        List<SettingChangeLogJpaEntity> entities = logs.stream()
                .map(SettingChangeLogJpaEntity::fromDomain)
                .toList();
        return springDataSettingChangeLogRepository.saveAll(entities).stream()
                .map(SettingChangeLogJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<SettingChangeLog> findAllByOrderByChangedAtDesc() {
        return springDataSettingChangeLogRepository.findAllByOrderByChangedAtDesc().stream()
                .map(SettingChangeLogJpaEntity::toDomain)
                .toList();
    }
}
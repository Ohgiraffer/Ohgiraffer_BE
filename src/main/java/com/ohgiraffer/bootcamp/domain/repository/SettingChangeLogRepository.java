package com.ohgiraffer.bootcamp.domain.repository;

import com.ohgiraffer.bootcamp.domain.model.SettingChangeLog;

import java.util.List;

public interface SettingChangeLogRepository {
    List<SettingChangeLog> saveAll(List<SettingChangeLog> logs);
}

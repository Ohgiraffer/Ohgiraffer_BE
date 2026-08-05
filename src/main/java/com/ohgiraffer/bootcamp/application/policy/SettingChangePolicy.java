package com.ohgiraffer.bootcamp.application.policy;

import com.ohgiraffer.bootcamp.domain.model.SettingChangeLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SettingChangePolicy {

    private SettingChangePolicy() {}

    public static List<SettingChangeLog> diff(Long changedBy, String field, Object oldValue, Object newValue) {
        List<SettingChangeLog> logs = new ArrayList<>();
        if (!Objects.equals(oldValue, newValue)) {
            logs.add(SettingChangeLog.create(
                    changedBy, field,
                    oldValue == null ? null : oldValue.toString(),
                    newValue == null ? null : newValue.toString()
            ));
        }
        return logs;
    }
}
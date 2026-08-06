package com.ohgiraffer.bootcamp.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SettingChangeLog {

    private final Long id;
    private final Long bootcampId;
    private final Long changedBy;
    private final String changedField;
    private final String oldValue;
    private final String newValue;
    private final LocalDateTime changedAt;

    private SettingChangeLog(Long id, Long bootcampId, Long changedBy, String changedField,
                             String oldValue, String newValue, LocalDateTime changedAt) {
        this.id = id;
        this.bootcampId = bootcampId;
        this.changedBy = changedBy;
        this.changedField = changedField;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedAt = changedAt;
    }

    public static SettingChangeLog create(Long bootcampId, Long changedBy, String changedField,
                                          String oldValue, String newValue) {
        return new SettingChangeLog(null, bootcampId, changedBy, changedField, oldValue, newValue, null);
    }

    public static SettingChangeLog reconstruct(Long id, Long bootcampId, Long changedBy, String changedField,
                                               String oldValue, String newValue, LocalDateTime changedAt) {
        return new SettingChangeLog(id, bootcampId, changedBy, changedField, oldValue, newValue, changedAt);
    }
}
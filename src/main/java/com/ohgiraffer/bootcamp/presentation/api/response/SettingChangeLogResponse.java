package com.ohgiraffer.bootcamp.presentation.api.response;

import java.time.LocalDateTime;
import java.util.List;

public record SettingChangeLogResponse(
        List<Item> logs
) {
    public record Item(
            String changedByName,
            LocalDateTime changedAt,
            String changedField,
            String oldValue,
            String newValue
    ) {}
}
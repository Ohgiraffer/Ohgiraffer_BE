package com.ohgiraffer.bootcamp.application.usecase;

import com.ohgiraffer.bootcamp.presentation.api.response.BootcampSettingsResponse;
import com.ohgiraffer.bootcamp.presentation.api.response.SettingChangeLogResponse;

public interface BootcampQueryUsecase {
    BootcampSettingsResponse getSettings(Long userId);
    SettingChangeLogResponse getSettingChangeLogs(Long userId);
}

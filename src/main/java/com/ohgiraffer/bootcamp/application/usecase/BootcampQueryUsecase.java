package com.ohgiraffer.bootcamp.application.usecase;

import com.ohgiraffer.bootcamp.domain.model.AttendancePolicyResult;
import com.ohgiraffer.bootcamp.domain.model.BootcampPeriodResult;
import com.ohgiraffer.bootcamp.presentation.api.response.BootcampSettingsResponse;
import com.ohgiraffer.bootcamp.presentation.api.response.SettingChangeLogResponse;

public interface BootcampQueryUsecase {
    BootcampSettingsResponse getSettings(Long userId);
    SettingChangeLogResponse getSettingChangeLogs(Long userId);
    BootcampPeriodResult getPeriod(Long bootcampId);
    AttendancePolicyResult getPolicy(Long bootcampId);
}

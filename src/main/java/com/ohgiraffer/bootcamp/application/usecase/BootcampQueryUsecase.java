package com.ohgiraffer.bootcamp.application.usecase;

import com.ohgiraffer.bootcamp.domain.model.AttendancePeriodResult;
import com.ohgiraffer.bootcamp.domain.model.AttendancePolicyResult;
import com.ohgiraffer.bootcamp.domain.model.BootcampPeriodResult;
import com.ohgiraffer.bootcamp.presentation.api.response.BootcampSettingsResponse;
import com.ohgiraffer.bootcamp.presentation.api.response.SettingChangeLogResponse;

import java.util.List;

public interface BootcampQueryUsecase {
    BootcampSettingsResponse getSettings(Long userId);
    SettingChangeLogResponse getSettingChangeLogs(Long userId);
    BootcampPeriodResult getPeriod(Long bootcampId);
    AttendancePolicyResult getPolicy(Long bootcampId);
    List<AttendancePeriodResult> getAttendancePeriods(Long bootcampId);
}

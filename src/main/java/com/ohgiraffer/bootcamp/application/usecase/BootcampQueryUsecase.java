package com.ohgiraffer.bootcamp.application.usecase;

import com.ohgiraffer.bootcamp.domain.model.AttendancePeriodResult;
import com.ohgiraffer.bootcamp.domain.model.AttendancePeriodStartResult;
import com.ohgiraffer.bootcamp.domain.model.AttendancePolicyResult;
import com.ohgiraffer.bootcamp.domain.model.BootcampPeriodResult;
import com.ohgiraffer.bootcamp.presentation.api.response.BootcampLoginBasicResponse;
import com.ohgiraffer.bootcamp.presentation.api.response.BootcampSettingsResponse;
import com.ohgiraffer.bootcamp.presentation.api.response.SettingChangeLogResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BootcampQueryUsecase {
    BootcampSettingsResponse getSettings(Long userId);
    SettingChangeLogResponse getSettingChangeLogs(Long userId);
    BootcampPeriodResult getPeriod(Long bootcampId);
    AttendancePolicyResult getPolicy(Long bootcampId);
    List<AttendancePeriodResult> getAttendancePeriods(Long bootcampId);
    BootcampLoginBasicResponse getBasicInfo(Long userId);
    List<AttendancePeriodStartResult> getPeriodsStartingOn(LocalDate date);
    List<AttendancePeriodStartResult> getActivePeriods(LocalDate referenceDate);
}

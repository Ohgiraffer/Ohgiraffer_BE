package com.ohgiraffer.bootcamp.application.service;

import com.ohgiraffer.bootcamp.application.port.GetUserBootcampIdPort;
import com.ohgiraffer.bootcamp.application.port.GetUserNamesPort;
import com.ohgiraffer.bootcamp.application.usecase.BootcampQueryUsecase;
import com.ohgiraffer.bootcamp.domain.model.*;
import com.ohgiraffer.bootcamp.domain.repository.AttendancePeriodRepository;
import com.ohgiraffer.bootcamp.domain.repository.AttendancePolicyRepository;
import com.ohgiraffer.bootcamp.domain.repository.BootcampRepository;
import com.ohgiraffer.bootcamp.domain.repository.SettingChangeLogRepository;
import com.ohgiraffer.bootcamp.presentation.api.response.BootcampLoginBasicResponse;
import com.ohgiraffer.bootcamp.presentation.api.response.BootcampSettingsResponse;
import com.ohgiraffer.bootcamp.presentation.api.response.SettingChangeLogResponse;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BootcampQueryService implements BootcampQueryUsecase {

    private final BootcampRepository bootcampRepository;
    private final AttendancePeriodRepository attendancePeriodRepository;
    private final AttendancePolicyRepository attendancePolicyRepository;
    private final GetUserBootcampIdPort getUserBootcampIdPort;
    private final SettingChangeLogRepository settingChangeLogRepository;
    private final GetUserNamesPort getUserNamesPort;

    @Override
    public BootcampSettingsResponse getSettings(Long userId) {
        Long bootcampId = getUserBootcampIdPort.findBootcampIdByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOTCAMP_NOT_FOUND));

        Bootcamp bootcamp = bootcampRepository.findById(bootcampId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOTCAMP_NOT_FOUND));

        List<AttendancePeriod> periods = attendancePeriodRepository.findAllByBootcampId(bootcampId);

        List<BootcampSettingsResponse.PeriodItem> periodItems = periods.stream()
                .map(p -> new BootcampSettingsResponse.PeriodItem(
                        p.getId(), p.getPeriodNo(), p.getPeriodStart(), p.getPeriodEnd()))
                .toList();

        return new BootcampSettingsResponse(
                bootcamp.getId(), bootcamp.getOrgName(), bootcamp.getProName(),
                bootcamp.getStartDate(), bootcamp.getEndDate(), periodItems
        );
    }

    @Override
    public SettingChangeLogResponse getSettingChangeLogs(Long userId) {
        Long bootcampId = getUserBootcampIdPort.findBootcampIdByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOTCAMP_NOT_FOUND));

        List<SettingChangeLog> logs = settingChangeLogRepository.findAllByBootcampIdOrderByChangedAtDesc(bootcampId);

        List<Long> userIds = logs.stream()
                .map(SettingChangeLog::getChangedBy)
                .distinct()
                .toList();
        Map<Long, String> nameByUserId = getUserNamesPort.findNamesByUserIds(userIds);

        List<SettingChangeLogResponse.Item> items = logs.stream()
                .map(l -> new SettingChangeLogResponse.Item(
                        nameByUserId.getOrDefault(l.getChangedBy(), "알 수 없음"),
                        l.getChangedAt(),
                        l.getChangedField(),
                        l.getOldValue(),
                        l.getNewValue()
                ))
                .toList();

        return new SettingChangeLogResponse(items);
    }

    @Override
    public BootcampPeriodResult getPeriod(Long bootcampId) {
        Bootcamp bootcamp = bootcampRepository.findById(bootcampId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOTCAMP_NOT_FOUND));
        return new BootcampPeriodResult(bootcamp.getStartDate(), bootcamp.getEndDate());
    }

    @Override
    public AttendancePolicyResult getPolicy(Long bootcampId) {
        AttendancePolicy policy = attendancePolicyRepository.findByBootcampId(bootcampId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOTCAMP_NOT_FOUND));

        return new AttendancePolicyResult(
                policy.getCautionThresholdPct(),
                policy.getWarningThresholdPct(),
                policy.getPeriodExpulsionPct()
        );
    }

    @Override
    public List<AttendancePeriodResult> getAttendancePeriods(Long bootcampId) {
        return attendancePeriodRepository.findAllByBootcampId(bootcampId).stream()
                .map(p -> new AttendancePeriodResult(p.getId(), p.getPeriodNo(), p.getPeriodStart(), p.getPeriodEnd()))
                .sorted(Comparator.comparing(AttendancePeriodResult::periodNo))
                .toList();
    }

    @Override
    public BootcampLoginBasicResponse getBasicInfo(Long userId) {
        Long bootcampId = getUserBootcampIdPort.findBootcampIdByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOTCAMP_NOT_FOUND));

        Bootcamp bootcamp = bootcampRepository.findById(bootcampId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOTCAMP_NOT_FOUND));

        return new BootcampLoginBasicResponse(bootcamp.getOrgName(), bootcamp.getProName());
    }

    @Override
    public List<AttendancePeriodStartResult> getPeriodsStartingOn(LocalDate date) {
        return attendancePeriodRepository.findAllByPeriodStart(date).stream()
                .map(p -> new AttendancePeriodStartResult(
                        p.getId(),
                        p.getBootcampId(),
                        p.getPeriodNo(),
                        p.getPeriodStart(),
                        p.getPeriodEnd()
                ))
                .toList();
    }

    @Override
    public List<AttendancePeriodStartResult> getActivePeriods(LocalDate referenceDate) {
        return attendancePeriodRepository.findActivePeriods(referenceDate).stream()
                .map(p -> new AttendancePeriodStartResult(
                        p.getId(),
                        p.getBootcampId(),
                        p.getPeriodNo(),
                        p.getPeriodStart(),
                        p.getPeriodEnd()
                ))
                .toList();
    }
}
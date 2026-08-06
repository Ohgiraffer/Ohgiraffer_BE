package com.ohgiraffer.bootcamp.application.service;

import com.ohgiraffer.bootcamp.application.command.PeriodCommand;
import com.ohgiraffer.bootcamp.application.policy.AttendancePeriodChangeLogPolicy;
import com.ohgiraffer.bootcamp.application.policy.AttendancePeriodPolicy;
import com.ohgiraffer.bootcamp.application.policy.BootcampInfoChangeLogPolicy;
import com.ohgiraffer.bootcamp.application.port.GetUserBootcampIdPort;
import com.ohgiraffer.bootcamp.application.port.SetBootcampIdPort;
import com.ohgiraffer.bootcamp.application.usecase.BootcampCommandUsecase;
import com.ohgiraffer.bootcamp.domain.model.AttendancePeriod;
import com.ohgiraffer.bootcamp.domain.model.AttendancePolicy;
import com.ohgiraffer.bootcamp.domain.model.Bootcamp;
import com.ohgiraffer.bootcamp.domain.model.SettingChangeLog;
import com.ohgiraffer.bootcamp.domain.repository.AttendancePeriodRepository;
import com.ohgiraffer.bootcamp.domain.repository.AttendancePolicyRepository;
import com.ohgiraffer.bootcamp.domain.repository.BootcampRepository;
import com.ohgiraffer.bootcamp.domain.repository.SettingChangeLogRepository;
import com.ohgiraffer.bootcamp.presentation.api.request.BootcampPolicyRequest;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class BootcampCommandService implements BootcampCommandUsecase {

    private final BootcampRepository bootcampRepository;
    private final AttendancePeriodRepository attendancePeriodRepository;
    private final AttendancePolicyRepository attendancePolicyRepository;
    private final GetUserBootcampIdPort getUserBootcampIdPort;
    private final SettingChangeLogRepository settingChangeLogRepository;
    private final SetBootcampIdPort setBootcampIdPort;

    @Override
    public Long register(Long userId, String orgName, String proName, LocalDate startDate, LocalDate endDate) {
        Bootcamp saved = bootcampRepository.save(Bootcamp.create(orgName, proName, startDate, endDate));

        setBootcampIdPort.assignBootcamp(userId, saved.getId());

        log.info("[register] 부트캠프 등록 완료 | bootcampId={}, orgName={}, userId={}", saved.getId(), saved.getOrgName(), userId);

        return saved.getId();
    }

    @Override
    public void update(Long bootcampId, String orgName, String proName, LocalDate startDate, LocalDate endDate) {
        Bootcamp bootcamp = bootcampRepository.findById(bootcampId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOTCAMP_NOT_FOUND));

        bootcamp.changeInfo(orgName, proName, startDate, endDate);
        bootcampRepository.save(bootcamp);

        log.info("[update] 부트캠프 수정 완료 | bootcampId={}, orgName={}", bootcampId, orgName);
    }

    @Override
    public void savePolicy(BootcampPolicyRequest request) {
        bootcampRepository.findById(request.bootcampId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOTCAMP_NOT_FOUND));

        List<AttendancePeriod> periods = request.periods().stream()
                .map(p -> AttendancePeriod.create(p.periodNo(), p.periodStart(), p.periodEnd(), request.bootcampId()))
                .toList();

        AttendancePeriodPolicy.validate(periods);
        attendancePeriodRepository.saveAll(periods);

        attendancePolicyRepository.save(AttendancePolicy.create(
                request.cautionPercent(),
                request.warningPercent(),
                request.expulsionPercent(),
                request.bootcampId()
        ));

        log.info("[savePolicy] 출결 정책 저장 완료 | bootcampId={}", request.bootcampId());
    }

    @Override
    public void updateSettings(Long userId, String orgName, String proName,
                               LocalDate startDate, LocalDate endDate,
                               List<PeriodCommand> periods) {
        Long bootcampId = getUserBootcampIdPort.findBootcampIdByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOTCAMP_NOT_FOUND));

        Bootcamp bootcamp = bootcampRepository.findById(bootcampId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOTCAMP_NOT_FOUND));

        List<AttendancePeriod> oldPeriods = attendancePeriodRepository.findAllByBootcampId(bootcampId);

        List<SettingChangeLog> logs = new ArrayList<>();
        logs.addAll(BootcampInfoChangeLogPolicy.diff(bootcampId, userId, bootcamp, orgName, proName, startDate, endDate));
        logs.addAll(AttendancePeriodChangeLogPolicy.diff(bootcampId, userId, oldPeriods, periods));

        bootcamp.changeInfo(orgName, proName, startDate, endDate);
        bootcampRepository.save(bootcamp);

        List<AttendancePeriod> newPeriods = periods.stream()
                .map(p -> AttendancePeriod.create(p.periodNo(), p.periodStart(), p.periodEnd(), bootcampId))
                .toList();
        AttendancePeriodPolicy.validate(newPeriods);

        attendancePeriodRepository.deleteAllByBootcampId(bootcampId);
        attendancePeriodRepository.saveAll(newPeriods);

        settingChangeLogRepository.saveAll(logs);

        log.info("[updateSettings] 부트캠프 설정 일괄 수정 완료 | userId={}, bootcampId={}, changedFieldCount={}",
                userId, bootcampId, logs.size());
    }
}
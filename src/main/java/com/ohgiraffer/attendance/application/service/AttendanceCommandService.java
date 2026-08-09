package com.ohgiraffer.attendance.application.service;

import com.ohgiraffer.attendance.application.cache.AttendanceCacheEvictor;
import com.ohgiraffer.attendance.application.usecase.AttendanceCommandUsecase;
import com.ohgiraffer.attendance.domain.model.Attendance;
import com.ohgiraffer.attendance.domain.model.AttendanceStatus;
import com.ohgiraffer.attendance.domain.policy.AttendanceMetricsCalculator;
import com.ohgiraffer.attendance.domain.repository.AttendanceRepository;
import com.ohgiraffer.attendance.domain.repository.LeaveBalanceRepository;
import com.ohgiraffer.bootcamp.application.usecase.BootcampQueryUsecase;
import com.ohgiraffer.bootcamp.domain.model.AttendancePeriodResult;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Transactional
@Slf4j
@RequiredArgsConstructor
@Service
public class AttendanceCommandService implements AttendanceCommandUsecase {

    private final AttendanceRepository attendanceRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final UserQueryUsecase userQueryUsecase;
    private final BootcampQueryUsecase bootcampQueryUsecase;
    private final AttendanceCacheEvictor attendanceCacheEvictor;

    @Override
    @Transactional
    public void applyApprovedLeave(Long userId, LocalDate startDate, LocalDate endDate, Long approvalId) {
        Long bootcampId = userQueryUsecase.getBootcampId(userId);
        List<AttendancePeriodResult> periods = bootcampQueryUsecase.getAttendancePeriods(bootcampId);
        String externalRefId = String.valueOf(approvalId);

        for (LocalDate date : AttendanceMetricsCalculator.weekdaysBetween(startDate, endDate)) {
            applyLeaveDay(userId, date, externalRefId, periods);
        }

        attendanceCacheEvictor.evictSummary(userId);
        attendanceCacheEvictor.evictAllForBootcamp(bootcampId);
    }

    private void applyLeaveDay(Long userId, LocalDate date, String externalRefId, List<AttendancePeriodResult> periods) {
        Optional<Attendance> existing = attendanceRepository.findByUserIdAndDate(userId, date);
        if (existing.isPresent() && externalRefId.equals(existing.get().getExternalRefId())) {
            return; // 재처리 방지
        }

        AttendancePeriodResult period = periods.stream()
                .filter(p -> !date.isBefore(p.periodStart()) && !date.isAfter(p.periodEnd()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_PERIOD_NOT_FOUND));

        boolean consumed = leaveBalanceRepository.tryConsume(userId, period.periodStart(), BigDecimal.ONE);
        if (!consumed) {
            throw new BusinessException(ErrorCode.LEAVE_BALANCE_NOT_ENOUGH);
        }

        Attendance attendance = existing.isPresent()
                ? Attendance.reconstitute(
                existing.get().getId(), userId, date, AttendanceStatus.LEAVE, null, null, externalRefId)
                : Attendance.create(userId, date, AttendanceStatus.LEAVE, null, null, externalRefId);

        attendanceRepository.save(attendance);
    }
}
package com.ohgiraffer.attendance.application.service;

import com.ohgiraffer.attendance.application.cache.AttendanceCacheEvictor;
import com.ohgiraffer.attendance.application.usecase.AttendanceCommandUsecase;
import com.ohgiraffer.attendance.domain.model.Attendance;
import com.ohgiraffer.attendance.domain.model.AttendanceStatus;
import com.ohgiraffer.attendance.domain.policy.AttendanceMetricsCalculator;
import com.ohgiraffer.attendance.domain.repository.AttendanceRepository;
import com.ohgiraffer.attendance.domain.repository.LeaveBalanceRepository;
import com.ohgiraffer.attendance.infrastructure.scheduler.AttendanceBalanceProcessor;
import com.ohgiraffer.bootcamp.application.usecase.BootcampQueryUsecase;
import com.ohgiraffer.bootcamp.domain.model.BootcampPeriodResult;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Transactional
@Slf4j
@RequiredArgsConstructor
@Service
public class AttendanceCommandService implements AttendanceCommandUsecase {

    private final AttendanceRepository attendanceRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final UserQueryUsecase userQueryUsecase;
    private final AttendanceCacheEvictor attendanceCacheEvictor;
    private final AttendanceBalanceProcessor attendanceBalanceProcessor;
    private final BootcampQueryUsecase bootcampQueryUsecase;

    @Override
    @Transactional
    public void applyApprovedLeave(Long userId, LocalDate startDate, LocalDate endDate, Long approvalId) {
        Long bootcampId = userQueryUsecase.getBootcampId(userId);
        String externalRefId = String.valueOf(approvalId);

        BootcampPeriodResult bootcampPeriod = bootcampQueryUsecase.getPeriod(bootcampId);
        attendanceBalanceProcessor.ensureLeaveBalance(userId, bootcampPeriod, LocalDate.now());

        for (LocalDate date : AttendanceMetricsCalculator.weekdaysBetween(startDate, endDate)) {
            applyLeaveDay(userId, date, externalRefId);
        }

        attendanceCacheEvictor.evictSummary(userId);
        attendanceCacheEvictor.evictAllForBootcamp(bootcampId);
    }


    private void applyLeaveDay(Long userId, LocalDate date, String externalRefId) {
        Optional<Attendance> existing = attendanceRepository.findByUserIdAndDateForUpdate(userId, date);

        if (existing.isPresent()) {
            Attendance existingAttendance = existing.get();

            if (externalRefId.equals(existingAttendance.getExternalRefId())) {
                return; // 같은 승인 건 재처리 방지
            }

            if (existingAttendance.getStatus() == AttendanceStatus.LEAVE
                    || existingAttendance.getStatus() == AttendanceStatus.SICK) {
                throw new BusinessException(ErrorCode.ATTENDANCE_APPROVAL_CONFLICT);
            }
        }

        Attendance attendance = existing.isPresent()
                ? Attendance.reconstitute(
                existing.get().getId(), userId, date, AttendanceStatus.LEAVE, null, null, null, null, externalRefId)
                : Attendance.create(userId, date, AttendanceStatus.LEAVE, null, null, null, null, externalRefId);

        try {
            attendanceRepository.save(attendance);
        } catch (DataIntegrityViolationException e) {
            Optional<Attendance> reloaded = attendanceRepository.findByUserIdAndDate(userId, date);
            if (reloaded.isPresent() && externalRefId.equals(reloaded.get().getExternalRefId())) {
                return;
            }
            throw new BusinessException(ErrorCode.ATTENDANCE_APPROVAL_CONFLICT, e);
        }

        boolean consumed = leaveBalanceRepository.tryConsume(userId, BigDecimal.ONE);
        if (!consumed) {
            throw new BusinessException(ErrorCode.LEAVE_BALANCE_NOT_ENOUGH);
        }
    }
}
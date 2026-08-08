package com.ohgiraffer.attendance.infrastructure.scheduler;

import com.ohgiraffer.bootcamp.application.usecase.BootcampQueryUsecase;
import com.ohgiraffer.bootcamp.domain.model.AttendancePeriodStartResult;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceBalanceScheduler {

    private final BootcampQueryUsecase bootcampQueryUsecase;
    private final UserQueryUsecase userQueryUsecase;
    private final AttendanceBalanceProcessor processor;

    @Scheduled(cron = "0 5 0 * * *")
    public void rolloverBalances() {
        LocalDate today = LocalDate.now();
        List<AttendancePeriodStartResult> startingPeriods = bootcampQueryUsecase.getPeriodsStartingOn(today);

        if (startingPeriods.isEmpty()) {
            log.info("[rolloverBalances] 오늘 시작하는 단위기간 없음 | date={}", today);
            return;
        }

        for (AttendancePeriodStartResult period : startingPeriods) {
            List<Long> studentIds = userQueryUsecase.getStudentIdsByBootcampId(period.bootcampId());
            for (Long studentId : studentIds) {
                try {
                    processor.processStudent(studentId, period);
                } catch (DataIntegrityViolationException e) {
                    log.warn("[rolloverBalances] 중복 키 — 이미 존재하는 잔액으로 처리 | studentId={}, periodNo={}",
                            studentId, period.periodNo());
                } catch (Exception e) {
                    log.error("[rolloverBalances] 처리 실패 | studentId={}, periodNo={}, error={}",
                            studentId, period.periodNo(), e.getMessage());
                }
            }
            log.info("[rolloverBalances] 이월 처리 완료 | bootcampId={}, periodNo={}, studentCount={}",
                    period.bootcampId(), period.periodNo(), studentIds.size());
        }
    }
}
package com.ohgiraffer.attendance.infrastructure.adapter;
import com.ohgiraffer.approval.application.port.ApplyApprovedLeavePort;
import com.ohgiraffer.attendance.application.usecase.AttendanceCommandUsecase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ApplyApprovedLeaveAdapter implements ApplyApprovedLeavePort {

    private final AttendanceCommandUsecase attendanceCommandUsecase;

    @Override
    public void applyApprovedLeave(Long userId, LocalDate startDate, LocalDate endDate, Long approvalId) {
        attendanceCommandUsecase.applyApprovedLeave(userId, startDate, endDate, approvalId);
    }
}
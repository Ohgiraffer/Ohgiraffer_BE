package com.ohgiraffer.attendance.application.service;

import com.ohgiraffer.attendance.application.usecase.AttendanceSheetQueryUsecase;
import com.ohgiraffer.attendance.domain.dto.AttendanceSheetSyncLogView;
import com.ohgiraffer.attendance.domain.repository.AttendanceSheetSyncLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AttendanceSheetQueryService implements AttendanceSheetQueryUsecase {

    private final AttendanceSheetSyncLogRepository attendanceSheetSyncLogRepository;

    @Override
    public List<AttendanceSheetSyncLogView> getLogs() {
        return attendanceSheetSyncLogRepository.findAll().stream()
                .map(AttendanceSheetSyncLogView::from)
                .toList();
    }
}
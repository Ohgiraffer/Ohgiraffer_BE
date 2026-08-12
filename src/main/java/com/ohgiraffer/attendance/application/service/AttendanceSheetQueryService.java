package com.ohgiraffer.attendance.application.service;

import com.ohgiraffer.attendance.application.usecase.AttendanceSheetQueryUsecase;
import com.ohgiraffer.attendance.domain.dto.AttendanceExternalSheetLinkView;
import com.ohgiraffer.attendance.domain.dto.AttendanceSheetSyncLogView;
import com.ohgiraffer.attendance.domain.repository.AttendanceExternalSheetLinkRepository;
import com.ohgiraffer.attendance.domain.repository.AttendanceSheetSyncLogRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AttendanceSheetQueryService implements AttendanceSheetQueryUsecase {

    private final AttendanceSheetSyncLogRepository attendanceSheetSyncLogRepository;
    private final AttendanceExternalSheetLinkRepository attendanceExternalSheetLinkRepository;

    @Override
    public List<AttendanceSheetSyncLogView> getLogs() {
        return attendanceSheetSyncLogRepository.findAll().stream()
                .map(AttendanceSheetSyncLogView::from)
                .toList();
    }

    @Override
    public AttendanceExternalSheetLinkView getSheetLink() {
        return attendanceExternalSheetLinkRepository.findLatest()
                .map(AttendanceExternalSheetLinkView::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_SHEET_LINK_NOT_FOUND));
    }
}
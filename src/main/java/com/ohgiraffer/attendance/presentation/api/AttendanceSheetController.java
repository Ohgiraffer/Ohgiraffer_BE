package com.ohgiraffer.attendance.presentation.api;

import com.ohgiraffer.attendance.application.command.SaveAttendanceExternalSheetLinkCommand;
import com.ohgiraffer.attendance.application.command.SyncAttendanceSheetCommand;
import com.ohgiraffer.attendance.application.usecase.AttendanceSheetCommandUsecase;
import com.ohgiraffer.attendance.application.usecase.AttendanceSheetQueryUsecase;
import com.ohgiraffer.attendance.domain.dto.AttendanceExternalSheetLinkView;
import com.ohgiraffer.attendance.domain.dto.AttendanceSheetSyncLogView;
import com.ohgiraffer.attendance.domain.dto.SyncAttendanceSheetResult;
import com.ohgiraffer.attendance.domain.model.SyncTriggerType;
import com.ohgiraffer.attendance.presentation.api.request.SaveAttendanceSheetLinkRequest;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance - 시트 연동/동기화", description = "출결 구글시트 연동 설정 저장, 동기화 실행, 동기화 이력 조회를 위한 컨트롤러")
public class AttendanceSheetController {

    private final AttendanceSheetCommandUsecase attendanceSheetCommandUsecase;
    private final AttendanceSheetQueryUsecase attendanceSheetQueryUsecase;

    @Operation(summary = "출결 시트 연동 설정 저장", description = "관리자가 시트 URL, 탭명, 날짜 선택 셀, 컬럼매핑을 저장합니다. 이미 저장된 설정이 있으면 덮어씁니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "요청값이 올바르지 않음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @PostMapping("/sheet-link")
    public ResponseEntity<Void> saveLink(@Valid @RequestBody SaveAttendanceSheetLinkRequest request) {
        attendanceSheetCommandUsecase.save(
                new SaveAttendanceExternalSheetLinkCommand(
                        request.sheetUrl(),
                        request.tabName(),
                        request.dateCellRange(),
                        request.columnMapping()
                )
        );
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "출결 시트 연동 설정 조회", description = "현재 저장된 시트 연동 설정을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "등록된 시트 연동 설정이 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @GetMapping("/sheet-link")
    public ResponseEntity<AttendanceExternalSheetLinkView> getSheetLink() {
        return ResponseEntity.ok(attendanceSheetQueryUsecase.getSheetLink());
    }


    @Operation(summary = "출결 시트 동기화 (관리자용)", description = "저장된 시트 연동 설정을 사용해 오늘 날짜의 출결 데이터를 동기화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "동기화 성공(일부 행 실패가 있어도 200이며, 응답의 failedRows로 확인)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "등록된 시트 연동 설정이 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류 또는 Google Sheets API 호출 실패")
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @PostMapping("/sheet-sync")
    public ResponseEntity<SyncAttendanceSheetResult> sync(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        SyncAttendanceSheetResult result = attendanceSheetCommandUsecase.sync(
                new SyncAttendanceSheetCommand(principal.getId(), SyncTriggerType.MANUAL)
        );
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "동기화 이력 전체 조회 (관리자용)", description = "최근 5일간의 동기화 이력을 최신순으로 전체 조회합니다. 5일이 지난 이력은 매일 자정 직후 자동 삭제됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @GetMapping("/sheet-sync/logs")
    public ResponseEntity<List<AttendanceSheetSyncLogView>> getLogs() {
        return ResponseEntity.ok(attendanceSheetQueryUsecase.getLogs());
    }
}
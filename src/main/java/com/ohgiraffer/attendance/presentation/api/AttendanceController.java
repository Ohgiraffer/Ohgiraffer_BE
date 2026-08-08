package com.ohgiraffer.attendance.presentation.api;

import com.ohgiraffer.attendance.application.usecase.AttendanceQueryUsecase;
import com.ohgiraffer.attendance.presentation.api.response.AttendanceBalanceResponse;
import com.ohgiraffer.attendance.presentation.api.response.MonthlyAttendanceResponse;
import com.ohgiraffer.attendance.presentation.api.response.AttendanceSummaryResponse;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;


@RestController
@RequiredArgsConstructor
@RequestMapping("/attendance")
@Tag(name="Attendance - 출결 정보 관리", description = "출결 정보와 설정을 다루기 위한 컨트롤러")
@Validated
public class AttendanceController {

    private final AttendanceQueryUsecase attendanceQueryUsecase;

    @Operation(summary = "월별 출결 캘린더 조회", description = "로그인한 사용자 본인의 한 달간 출결 정보를 날짜별 상태 그룹으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "year/month 형식이 올바르지 않음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/monthly")
    public ResponseEntity<MonthlyAttendanceResponse> getMyCalendar(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam @Min(2020) @Max(2100) int year,
            @RequestParam @Min(1) @Max(12) int month
    ) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return ResponseEntity.ok(attendanceQueryUsecase.getMonthlyAttendance(principal.getId(), yearMonth));
    }

    @Operation(summary = "특정 학생 월별 출결 캘린더 조회 (관리자용)", description = "매니저가 같은 부트캠프 소속 학생의 한 달간 출결 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "year/month 값이 올바르지 않음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "403", description = "권한 없음 또는 다른 부트캠프 소속 학생"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 학생"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @GetMapping("/monthly/{studentId}")
    public ResponseEntity<MonthlyAttendanceResponse> getStudentCalendar(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long studentId,
            @RequestParam @Min(2020) @Max(2100) int year,
            @RequestParam @Min(1) @Max(12) int month
    ) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return ResponseEntity.ok(
                attendanceQueryUsecase.getMonthlyAttendanceForManager(principal.getId(), studentId, yearMonth)
        );
    }

    @Operation(summary = "누적 출결 통계 조회", description = "로그인한 사용자 본인의 부트캠프 시작일부터 오늘까지 누적 출결 통계, 출석률, 위험도를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/summary")
    public ResponseEntity<AttendanceSummaryResponse> getMySummary(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(attendanceQueryUsecase.getSummary(principal.getId()));
    }

    @Operation(summary = "특정 학생 누적 출결 통계 조회 (관리자용)", description = "매니저가 같은 부트캠프 소속 학생의 누적 출결 통계를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "403", description = "권한 없음 또는 다른 부트캠프 소속 학생"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 학생"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @GetMapping("/summary/{studentId}")
    public ResponseEntity<AttendanceSummaryResponse> getStudentSummary(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long studentId
    ) {
        return ResponseEntity.ok(attendanceQueryUsecase.getSummaryForManager(principal.getId(), studentId));
    }

    @Operation(summary = "잔여 휴가/병결 조회", description = "로그인한 사용자 본인의 잔여 휴가일수와 병결일수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "404", description = "잔여 휴가/병결 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/leave-sick/count")
    public ResponseEntity<AttendanceBalanceResponse> getMyLeaveBalance(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(attendanceQueryUsecase.getLeaveBalance(principal.getId()));
    }

    @Operation(summary = "특정 학생 잔여 휴가/병결 조회 (관리자용)", description = "매니저/강사가 같은 부트캠프 소속 학생의 잔여 휴가일수와 병결일수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "403", description = "권한 없음 또는 다른 부트캠프 소속 학생"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 학생 또는 잔여 휴가/병결 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @GetMapping("/leave-sick/count/{studentId}")
    public ResponseEntity<AttendanceBalanceResponse> getStudentLeaveBalance(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long studentId
    ) {
        return ResponseEntity.ok(
                attendanceQueryUsecase.getLeaveBalanceForManager(principal.getId(), studentId)
        );
    }
}
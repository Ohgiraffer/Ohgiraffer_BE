package com.ohgiraffer.attendance.presentation.api;

import com.ohgiraffer.attendance.application.usecase.AttendanceQueryUsecase;
import com.ohgiraffer.attendance.presentation.api.response.MonthlyAttendanceResponse;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
            @RequestParam int year,
            @RequestParam @Min(1) @Max(12) int month
    ) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return ResponseEntity.ok(attendanceQueryUsecase.getMonthlyAttendance(principal.getId(), yearMonth));
    }
}
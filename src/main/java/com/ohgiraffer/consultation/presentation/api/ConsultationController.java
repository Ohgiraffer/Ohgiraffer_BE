package com.ohgiraffer.consultation.presentation.api;

import com.ohgiraffer.consultation.application.command.RegisterAvailableTimeCommand;
import com.ohgiraffer.consultation.application.command.RequestConsultationCommand;
import com.ohgiraffer.consultation.application.command.SaveRecordCommand;
import com.ohgiraffer.consultation.domain.model.ConsultationDetail;
import com.ohgiraffer.consultation.application.usecase.ConsultationCommandUsecase;
import com.ohgiraffer.consultation.application.usecase.ConsultationQueryUsecase;
import com.ohgiraffer.consultation.presentation.api.request.RegisterAvailableTimeRequest;
import com.ohgiraffer.consultation.presentation.api.request.RequestConsultationRequest;
import com.ohgiraffer.consultation.presentation.api.request.SaveConsultationRecordRequest;
import com.ohgiraffer.consultation.presentation.api.response.*;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/consultation")
@Tag(name = "Consultation - 상담 정보 관리", description = "상담 정보와 설정을 다루기 위한 컨트롤러")
public class ConsultationController {

    private final ConsultationCommandUsecase consultationCommandUsecase;
    private final ConsultationQueryUsecase consultationQueryUsecase;

    @Operation(summary = "상담자 목록 조회", description = "상담 신청 화면에서 선택 가능한 강사/매니저 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/counselors")
    public ResponseEntity<List<CounselorResponse>> getCounselors() {
        List<CounselorResponse> response = consultationQueryUsecase.getCounselors().stream()
                .map(CounselorResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "월별 상담 가능일 조회", description = "선택한 상담자의 해당 월 상담 가능일(설정됨 표시용) 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/available-dates")
    public ResponseEntity<Set<LocalDate>> getAvailableDates(
            @RequestParam Long counselorId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth
    ) {
        return ResponseEntity.ok(consultationQueryUsecase.getAvailableDates(counselorId, yearMonth));
    }

    @Operation(summary = "특정일 상담 가능 시간 조회", description = "등록된 상담 가능 시간 전체를 예약 여부(isReserved)와 함께 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/available-times")
    public ResponseEntity<List<AvailableTimeResponse>> getAvailableTimes(
            @RequestParam Long counselorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<AvailableTimeResponse> response = consultationQueryUsecase.getAvailableTimes(counselorId, date).stream()
                .map(AvailableTimeResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "상담 신청", description = "훈련생이 상담자·일시·주제를 선택해 상담을 신청합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "신청 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값이 올바르지 않음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "409", description = "이미 예약된 시간"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasAnyRole( 'STUDENT')")
    @PostMapping
    public ResponseEntity<Long> requestConsultation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody RequestConsultationRequest request
    ) {
        RequestConsultationCommand command = new RequestConsultationCommand(
                principal.getId(),
                request.counselorId(),
                request.scheduledAt(),
                request.topic(),
                request.content()
        );

        return ResponseEntity.ok(consultationCommandUsecase.requestConsultation(command));
    }

    @Operation(summary = "내 상담 이력 조회", description = "로그인한 훈련생 본인이 신청한 상담 이력을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasAnyRole('STUDENT')")
    @GetMapping("/my")
    public ResponseEntity<List<ConsultationSummaryResponse>> getMyConsultations(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<ConsultationSummaryResponse> response = consultationQueryUsecase.getMyConsultations(principal.getId()).stream()
                .map(ConsultationSummaryResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "상담 상세 조회", description = "상담 1건의 신청자·담당자·요청 내용·상담 기록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 상담"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{consultationId}")
    public ResponseEntity<ConsultationDetailResponse> getDetail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long consultationId
    ) {
        ConsultationDetail detail = consultationQueryUsecase.getDetail(
                consultationId, principal.getId(), principal.getRole().name());
        return ResponseEntity.ok(ConsultationDetailResponse.from(detail));
    }

    @Operation(summary = "다가오는 상담 조회", description = "운영진/강사가 예정된 상담 상위 3건을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @GetMapping("/upcoming")
    public ResponseEntity<List<ConsultationListItemResponse>> getUpcoming() {
        List<ConsultationListItemResponse> response = consultationQueryUsecase.getUpcoming().stream()
                .map(ConsultationListItemResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "전체 상담 이력 조회", description = "운영진/강사가 전체 상담 이력을 조회합니다. 필터링은 프론트에서 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @GetMapping("/history")
    public ResponseEntity<List<ConsultationListItemResponse>> getHistory() {
        List<ConsultationListItemResponse> response = consultationQueryUsecase.getHistory().stream()
                .map(ConsultationListItemResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "상담 기록 저장", description = "상담 종료 후 담당자가 상담 기록을 남기고 완료 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 상담"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @PatchMapping("/{consultationId}/record")
    public ResponseEntity<Void> saveRecord(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long consultationId,
            @Valid @RequestBody SaveConsultationRecordRequest request
    ) {
        consultationCommandUsecase.saveRecord(
                new SaveRecordCommand(consultationId, principal.getId(), request.counselorNote()));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내가 등록한 상담 가능 시간 조회", description = "가능 시간 등록 화면에서 특정일에 이미 등록된 시간을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @GetMapping("/available-times/mine")
    public ResponseEntity<List<String>> getRegisteredTimes(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<String> times = consultationQueryUsecase.getRegisteredTimes(principal.getId(), date).stream()
                .map(t -> t.format(DateTimeFormatter.ofPattern("HH:mm")))
                .toList();
        return ResponseEntity.ok(times);
    }

    @Operation(summary = "상담 가능 시간 등록/수정", description = "특정일의 상담 가능 시간을 전체 교체 방식으로 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값이 올바르지 않음(09:00~19:00, 30분 단위 벗어남)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @PutMapping("/available-times")
    public ResponseEntity<Void> registerAvailableTimes(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody RegisterAvailableTimeRequest request
    ) {
        consultationCommandUsecase.registerAvailableTime(
                new RegisterAvailableTimeCommand(principal.getId(), request.date(), request.times()));
        return ResponseEntity.ok().build();
    }
}
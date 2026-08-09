package com.ohgiraffer.bootcamp.presentation.api;

import com.ohgiraffer.bootcamp.application.command.PeriodCommand;
import com.ohgiraffer.bootcamp.application.usecase.BootcampCommandUsecase;
import com.ohgiraffer.bootcamp.application.usecase.BootcampQueryUsecase;
import com.ohgiraffer.bootcamp.presentation.api.request.BootcampInfoRequest;
import com.ohgiraffer.bootcamp.presentation.api.request.BootcampPolicyRequest;
import com.ohgiraffer.bootcamp.presentation.api.request.BootcampSettingsUpdateRequest;
import com.ohgiraffer.bootcamp.presentation.api.request.BootcampUpdateRequest;
import com.ohgiraffer.bootcamp.presentation.api.response.BootcampInfoResponse;
import com.ohgiraffer.bootcamp.presentation.api.response.BootcampLoginBasicResponse;
import com.ohgiraffer.bootcamp.presentation.api.response.BootcampSettingsResponse;
import com.ohgiraffer.bootcamp.presentation.api.response.SettingChangeLogResponse;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bootcamp")
@Tag(name="Bootcamp - 부트캠프 정보 관리", description = "부트캠프 정보와 정책을 다루기 위한 컨트롤러")
public class BootcampController {

    private final BootcampCommandUsecase bootcampCommandUsecase;
    private final BootcampQueryUsecase bootcampQueryUsecase;


    @Operation(summary = "부트캠프 정보 등록", description = "온보딩 1단계(조직·과정 정보)를 최초 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패 (필수값 누락, 종료일이 시작일보다 빠름)"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/info")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<BootcampInfoResponse> register(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody BootcampInfoRequest request
    ) {
        Long bootcampId = bootcampCommandUsecase.register(
                principal.getId(), request.orgName(), request.proName(), request.startDate(), request.endDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(new BootcampInfoResponse(bootcampId));
    }

    @Operation(summary = "부트캠프 정보 수정", description = "온보딩 1단계 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패 (필수값 누락, 종료일이 시작일보다 빠름)"),
            @ApiResponse(responseCode = "404", description = "부트캠프를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("/info")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> update(
            @Valid @RequestBody BootcampUpdateRequest request
    ) {
        bootcampCommandUsecase.update(
                request.bootcampId(), request.orgName(), request.proName(), request.startDate(), request.endDate());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "출결 정책 및 단위기간 등록", description = "온보딩 2·3단계 완료 시 단위기간과 출결 정책을 한 번에 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @ApiResponse(responseCode = "404", description = "부트캠프를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/policy")
    public ResponseEntity<Void> savePolicy(
            @Valid @RequestBody BootcampPolicyRequest request
    ) {
        bootcampCommandUsecase.savePolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "부트캠프 설정 조회", description = "관리자 설정 화면 — 로그인한 매니저의 부트캠프 정보와 단위기간 목록을 한 번에 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "부트캠프를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/settings")
    public ResponseEntity<BootcampSettingsResponse> getSettings(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(bootcampQueryUsecase.getSettings(principal.getId()));
    }

    @Operation(summary = "부트캠프 설정 일괄 수정", description = "관리자 설정 화면 저장 — 부트캠프 정보와 단위기간 전체를 replace 방식으로 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패 (필수값 누락, 단위기간 겹침, periodNo 중복, 종료일이 시작일보다 빠름)"),
            @ApiResponse(responseCode = "404", description = "부트캠프를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasRole('MANAGER')")
    @PatchMapping("/settings")
    public ResponseEntity<Void> updateSettings(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody BootcampSettingsUpdateRequest request
    ) {
        List<PeriodCommand> periods = request.periods().stream()
                .map(p -> new PeriodCommand(p.periodNo(), p.periodStart(), p.periodEnd()))
                .toList();

        bootcampCommandUsecase.updateSettings(
                principal.getId(), request.orgName(), request.proName(),
                request.startDate(), request.endDate(), periods);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "설정 변경 이력 조회", description = "관리자 설정 화면에서 발생한 모든 변경 이력을 최신순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "부트캠프를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/settings/logs")
    public ResponseEntity<SettingChangeLogResponse> getSettingChangeLogs(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(bootcampQueryUsecase.getSettingChangeLogs(principal.getId()));
    }

    @Operation(summary = "내 부트캠프 기본 정보 조회", description = "로그인한 사용자 본인이 속한 부트캠프의 조직명과 과정명을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "404", description = "부트캠프를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/basic")
    public ResponseEntity<BootcampLoginBasicResponse> getMyBootcampInfo(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(bootcampQueryUsecase.getBasicInfo(principal.getId()));
    }
}
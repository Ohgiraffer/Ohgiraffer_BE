package com.ohgiraffer.bootcamp.presentation.api;

import com.ohgiraffer.bootcamp.application.usecase.BootcampCommandUsecase;
import com.ohgiraffer.bootcamp.presentation.api.request.BootcampInfoRequest;
import com.ohgiraffer.bootcamp.presentation.api.request.BootcampPolicyRequest;
import com.ohgiraffer.bootcamp.presentation.api.request.BootcampUpdateRequest;
import com.ohgiraffer.bootcamp.presentation.api.response.BootcampInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bootcamp")
@Tag(name="Bootcamp - 부트캠프 정보 관리", description = "부트캠프 정보와 정책을 다루기 위한 컨트롤러")
public class BootcampController {

    private final BootcampCommandUsecase bootcampCommandUsecase;


    @Operation(summary = "부트캠프 정보 등록", description = "온보딩 1단계(조직·과정 정보)를 최초 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패 (필수값 누락, 종료일이 시작일보다 빠름)"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/info")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<BootcampInfoResponse> register(
            @Valid @RequestBody BootcampInfoRequest request
    ) {
        Long bootcampId = bootcampCommandUsecase.register(
                request.orgName(), request.proName(), request.startDate(), request.endDate());
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
}

package com.ohgiraffer.user.presentation.api;

import com.ohgiraffer.chat.application.usecase.ChatUserQueryUseCase;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import com.ohgiraffer.user.application.usecase.UserCommandUsecase;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import com.ohgiraffer.user.presentation.api.request.SetPasswordRequest;
import com.ohgiraffer.user.presentation.api.request.UserStatusChangeRequest;
import com.ohgiraffer.user.presentation.api.response.SetAlarmResponse;
import com.ohgiraffer.user.presentation.api.response.SetPasswordResponse;
import com.ohgiraffer.user.presentation.api.response.SetProfileImgResponse;
import com.ohgiraffer.user.presentation.api.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name="User - 사용자 정보 관리", description = "user 정보와 설정을 다루기 위한 User api 관련 컨트롤러")
public class UserController {

    private final UserCommandUsecase userCommandUsecase;
    private final UserQueryUsecase userQueryUsecase;

    @Operation(summary = "최초 비밀번호 재설정", description = "최초 로그인 시 임시 비밀번호를 새 비밀번호로 변경합니다. 변경 후에는 재로그인이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "비밀번호 형식이 올바르지 않음 (영문+특수기호 포함 8~16자)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "403", description = "비밀번호 재설정이 필요한 계정이 아님"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("/pw-reset")
    public ResponseEntity<SetPasswordResponse> changePassword(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestHeader("Authorization") String bearerToken,
            @Valid @RequestBody SetPasswordRequest request
    ) {
        userCommandUsecase.changePassword(principal.getId(), bearerToken, request.newPassword());
        return ResponseEntity.ok(SetPasswordResponse.of(false));
    }

    @Operation(summary = "알림 설정 토글", description = "호출할 때마다 개인 알림 수신 여부를 on/off로 전환합니다. 기본값은 on입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 설정 변경 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("/alarm-setting")
    public ResponseEntity<SetAlarmResponse> toggleNotification(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        boolean result = userCommandUsecase.setAlarm(principal.getId());
        return ResponseEntity.ok(new SetAlarmResponse(result));
    }

    @Operation(summary = "프로필 이미지 등록/수정", description = "프로필 이미지를 업로드합니다. 기존 이미지가 있으면 자동으로 교체됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 이미지 변경 성공"),
            @ApiResponse(responseCode = "400", description = "업로드할 이미지가 없거나(USER_003), 형식이 JPG/PNG가 아님(USER_005)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "413", description = "파일 크기가 50MB를 초과함(USER_004)"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SetProfileImgResponse> updateProfileImage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam("profileImg") MultipartFile profileImg
    ) {
        String url = userCommandUsecase.updateProfileImg(principal.getId(), profileImg);
        return ResponseEntity.ok(new SetProfileImgResponse(url));
    }

    @Operation(summary = "프로필 이미지 삭제", description = "등록된 프로필 이미지를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "프로필 이미지 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/profile-image")
    public ResponseEntity<Void> deleteProfileImage(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        userCommandUsecase.deleteProfileImg(principal.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자 본인의 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(userQueryUsecase.getMyInfo(principal.getId()));
    }

    @Operation(summary = "사용자 상태 변경(제적/자퇴)", description = "관리자가 특정 사용자의 상태를 제적 또는 자퇴로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "허용되지 않는 상태값"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 재원 상태가 아님"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasRole('MANAGER')")
    @PatchMapping("/status")
    public ResponseEntity<Void> changeUserStatus(
            @Valid @RequestBody UserStatusChangeRequest request
    ) {
        userCommandUsecase.changeUserStatus(request.userId(), request.status());
        return ResponseEntity.ok().build();
    }
}

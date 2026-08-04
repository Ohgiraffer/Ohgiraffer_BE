package com.ohgiraffer.auth.presentation.api;

import com.ohgiraffer.auth.application.usecase.AuthCommandUsecase;
import com.ohgiraffer.auth.presentation.api.request.LoginRequest;
import com.ohgiraffer.auth.presentation.api.response.LoginResponse;
import com.ohgiraffer.global.web.ClientIpResolver;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name="Auth - 인증·인가 ", description = "인증·인가를 위한 Auth api 관련 컨트롤러")
public class AuthController {
    private final AuthCommandUsecase authCommandUsecase;

    @Operation(summary = "로그인", description = "아이디와 비밀번호를 기입하여 로그인 합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호 불일치"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpRequest){

        String clientIp = ClientIpResolver.resolve(httpRequest);
        return ResponseEntity.ok(authCommandUsecase.login(request, clientIp));
    }


    @Operation(summary = "로그아웃", description = "로그아웃을 진행하면서 refresh token을 삭제합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 refresh token"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestHeader(value = "Refresh-Token", required = false) String refreshToken,
            @AuthenticationPrincipal CustomUserPrincipal principal
            ){

        authCommandUsecase.logout(principal.getId(),bearerToken,refreshToken);
        return ResponseEntity.noContent().build();
    }

}

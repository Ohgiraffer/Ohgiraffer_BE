package com.ohgiraffer.auth.presentation.api;

import com.ohgiraffer.auth.application.usecase.AuthCommandUsecase;
import com.ohgiraffer.auth.application.usecase.AuthQueryUsecase;
import com.ohgiraffer.auth.presentation.api.request.LoginRequest;
import com.ohgiraffer.auth.presentation.api.response.LoginResponse;
import com.ohgiraffer.global.web.ClientIpResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name="Auth - 인증·인가 ", description = "인증·인가를 위한 Auth api 관련 컨트롤러")
public class AuthController {
    private final AuthCommandUsecase authCommandUsecase;
    private final AuthQueryUsecase authQueryUsecase;

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

}

package com.ohgiraffer.security.handler;

import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.exception.ErrorResponse;
import com.ohgiraffer.global.trace.TraceIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        ErrorCode errorCode = Boolean.TRUE.equals(request.getAttribute("ALREADY_LOGGED_OUT"))
                ? ErrorCode.ALREADY_LOGGED_OUT
                : ErrorCode.UNAUTHORIZED;

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode,
                errorCode.getMessage(),
                request.getRequestURI(),
                (String) request.getAttribute(TraceIdFilter.TRACE_ID)
        );

        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}

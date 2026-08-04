package com.ohgiraffer.security.handler;

import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.exception.ErrorResponse;
import com.ohgiraffer.global.trace.TraceIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        ErrorCode errorCode = ErrorCode.FORBIDDEN;

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
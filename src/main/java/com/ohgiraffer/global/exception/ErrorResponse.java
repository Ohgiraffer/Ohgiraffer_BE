package com.ohgiraffer.global.exception;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        HttpStatus status,
        String code,
        String message,
        String path,
        String traceId,
        Map<String, String> errors
) {

    public static ErrorResponse of(
            ErrorCode errorCode,
            String message,
            String path,
            String traceId
    ) {
        return new ErrorResponse(
                Instant.now(),
                errorCode.getStatus(),
                errorCode.getCode(),
                message,
                path,
                traceId,
                Map.of()
        );
    }

    public static ErrorResponse of(
            ErrorCode errorCode,
            String message,
            String path
    ) {
        return of(errorCode, message, path, null, Map.of());
    }

    public static ErrorResponse of(
            ErrorCode errorCode,
            String message,
            String path,
            Map<String, String> errors
    ) {
        return of(errorCode, message, path, null, errors);
    }

    public static ErrorResponse of(
            ErrorCode errorCode,
            String message,
            String path,
            String traceId,
            Map<String, String> errors
    ) {
        return new ErrorResponse(
                Instant.now(),
                errorCode.getStatus(),
                errorCode.getCode(),
                message,
                path,
                traceId,
                errors == null ? Map.of() : Map.copyOf(errors)
        );
    }
}
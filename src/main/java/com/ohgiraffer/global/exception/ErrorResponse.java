package com.ohgiraffer.global.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        Map<String, String> errors
) {

    public static ErrorResponse of(
            ErrorCode errorCode,
            String message,
            String path
    ) {
        return new ErrorResponse(
                Instant.now(),
                errorCode.getStatus().value(),
                errorCode.getCode(),
                message,
                path,
                Map.of()
        );
    }

    public static ErrorResponse of(
            ErrorCode errorCode,
            String message,
            String path,
            Map<String, String> errors
    ) {
        return new ErrorResponse(
                Instant.now(),
                errorCode.getStatus().value(),
                errorCode.getCode(),
                message,
                path,
                errors == null ? Map.of() : Map.copyOf(errors)
        );
    }
}
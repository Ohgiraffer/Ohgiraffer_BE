package com.ohgiraffer.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import com.ohgiraffer.global.trace.TraceIdFilter;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /*
     * 서비스에서 의도적으로 발생시킨 비즈니스 예외
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = exception.getErrorCode();

        log.warn(
                "Business exception. code={} message={} path={}",
                errorCode.getCode(),
                exception.getMessage(),
                request.getRequestURI()
        );

        return createResponse(
                errorCode,
                exception.getMessage(),
                request
        );
    }

    /*
     * @RequestBody DTO의 @Valid 검증 실패
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError fieldError
                : exception.getBindingResult().getFieldErrors()) {

            errors.putIfAbsent(
                    fieldError.getField(),
                    getValidationMessage(fieldError)
            );
        }

        return createResponse(
                ErrorCode.INVALID_INPUT_VALUE,
                ErrorCode.INVALID_INPUT_VALUE.getMessage(),
                errors,
                request
        );
    }

    /*
     * @RequestParam, @PathVariable의 직접 검증 실패
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
public ResponseEntity<ErrorResponse> handleMethodValidation(
        HandlerMethodValidationException exception,
        HttpServletRequest request
) {
    if (exception.isForReturnValue()) {
        log.error(
                "Return value validation failed. path={}",
                request.getRequestURI(),
                exception
        );

        return createResponse(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                request
        );
    }

    log.warn(
            "Request parameter validation failed. path={} message={}",
            request.getRequestURI(),
            exception.getMessage()
    );

    return createResponse(
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.INVALID_INPUT_VALUE.getMessage(),
            request
    );
}

    /*
     * 서비스 또는 메서드 Validation 검증 실패
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getConstraintViolations()
                .forEach(violation ->
                        errors.putIfAbsent(
                                violation
                                        .getPropertyPath()
                                        .toString(),
                                violation.getMessage()
                        )
                );

        return createResponse(
                ErrorCode.INVALID_INPUT_VALUE,
                ErrorCode.INVALID_INPUT_VALUE.getMessage(),
                errors,
                request
        );
    }

    /*
     * 잘못된 JSON 또는 enum 변환 실패
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Request body is not readable. path={}",
                request.getRequestURI()
        );

        return createResponse(
                ErrorCode.INVALID_REQUEST_BODY,
                ErrorCode.INVALID_REQUEST_BODY.getMessage(),
                request
        );
    }

    /*
     * 필수 Query Parameter 누락
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        String message =
                exception.getParameterName()
                        + " 파라미터는 필수입니다.";

        return createResponse(
                ErrorCode.MISSING_REQUEST_PARAMETER,
                message,
                request
        );
    }

    /*
     * Query Parameter 또는 PathVariable 타입 불일치
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        String message =
                exception.getName()
                        + " 파라미터의 형식이 올바르지 않습니다.";

        return createResponse(
                ErrorCode.TYPE_MISMATCH,
                message,
                request
        );
    }

    /*
     * 지원하지 않는 HTTP Method
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        return createResponse(
                ErrorCode.METHOD_NOT_ALLOWED,
                ErrorCode.METHOD_NOT_ALLOWED.getMessage(),
                request
        );
    }

    /*
     * 존재하지 않는 URL 요청
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        return createResponse(
                ErrorCode.RESOURCE_NOT_FOUND,
                ErrorCode.RESOURCE_NOT_FOUND.getMessage(),
                request
        );
    }

    /*
     * 인증 실패 (토큰 없음, 만료 등) - 401
     */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            org.springframework.security.core.AuthenticationException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Authentication failed. path={}",
                request.getRequestURI()
        );

        return createResponse(
                ErrorCode.UNAUTHORIZED,
                ErrorCode.UNAUTHORIZED.getMessage(),
                request
        );
    }

    /*
     * 권한 없음 (인증은 됐지만 권한 부족) - 403
     */
    @ExceptionHandler(org.springframework.security.authorization.AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(
            org.springframework.security.authorization.AuthorizationDeniedException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Authorization denied. path={}",
                request.getRequestURI()
        );

        return createResponse(
                ErrorCode.FORBIDDEN,
                ErrorCode.FORBIDDEN.getMessage(),
                request
        );
    }

    /*
     * 예상하지 못한 서버 오류
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error(
                "Unexpected exception occurred. path={}",
                request.getRequestURI(),
                exception
        );

        return createResponse(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                request
        );
    }

    private String getValidationMessage(
            FieldError fieldError
    ) {
        String message = fieldError.getDefaultMessage();

        if (message == null || message.isBlank()) {
            return ErrorCode.INVALID_INPUT_VALUE.getMessage();
        }

        return message;
    }

    private ResponseEntity<ErrorResponse> createResponse(
            ErrorCode errorCode,
            String message,
            HttpServletRequest request
    ) {
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                message,
                request.getRequestURI(),
                MDC.get(TraceIdFilter.TRACE_ID),
                Map.of()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    private ResponseEntity<ErrorResponse> createResponse(
            ErrorCode errorCode,
            String message,
            Map<String, String> errors,
            HttpServletRequest request
    ) {
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                message,
                request.getRequestURI(),
                MDC.get(TraceIdFilter.TRACE_ID),
                errors
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }
}

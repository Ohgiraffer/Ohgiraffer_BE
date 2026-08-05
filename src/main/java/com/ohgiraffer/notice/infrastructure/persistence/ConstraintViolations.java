package com.ohgiraffer.notice.infrastructure.persistence;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * DB 제약 위반을 업무 오류로 바꾼다.
 *
 * <p>공지 도메인은 "먼저 확인하고 실행"하는 흐름이 여럿인데, 확인과 실행 사이에 다른 요청이
 * 끼어들면 DB 제약만 남는다. 그대로 두면 공통 예외 처리가 500 으로 내보내므로,
 * 평상시와 같은 응답이 나가도록 여기서 바꿔 준다.
 */
final class ConstraintViolations {

    /** V20260804_1142 마이그레이션에서 notice_category.name 에 건 UNIQUE 제약. */
    static final String UNIQUE_CATEGORY_NAME = "UQ_NOTICE_CATEGORY_NAME";

    /** baseline 스키마에서 notice.notice_category_id 에 건 외래키. */
    static final String NOTICE_TO_CATEGORY = "FK_notice_category_TO_notice_1";

    private ConstraintViolations() {
    }

    /**
     * 지정한 제약을 어긴 경우만 업무 오류로 바꾸고, 다른 무결성 오류는 원래대로 돌려준다.
     *
     * <p>제약 이름으로 가리는 이유는 한 테이블에 제약이 여럿이기 때문이다.
     * notice 만 해도 카테고리와 작성자 두 외래키가 있어, 뭉뚱그리면 엉뚱한 원인을 알리게 된다.
     *
     * @param message null 이면 ErrorCode 의 기본 문구를 쓴다
     * @return 바꾼 업무 오류, 또는 해당 제약이 아니면 받은 예외 그대로
     */
    static RuntimeException translate(
            DataIntegrityViolationException exception,
            String constraintName,
            ErrorCode errorCode,
            String message
    ) {
        if (!violates(exception, constraintName)) {
            return exception;
        }

        return message == null
                ? new BusinessException(errorCode)
                : new BusinessException(errorCode, message);
    }

    /**
     * 제약 이름은 드라이버가 감싼 예외 안쪽에 들어 있어 원인 사슬을 따라 내려가며 찾는다.
     */
    private static boolean violates(
            Throwable exception,
            String constraintName
    ) {
        Throwable cause = exception;

        while (cause != null) {
            String message = cause.getMessage();

            if (message != null && message.contains(constraintName)) {
                return true;
            }

            cause = cause.getCause() == cause ? null : cause.getCause();
        }

        return false;
    }
}

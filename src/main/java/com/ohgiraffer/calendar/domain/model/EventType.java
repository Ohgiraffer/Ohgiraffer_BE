package com.ohgiraffer.calendar.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.util.Arrays;

/**
 * 캘린더 일정 유형.
 *
 * <p>화면의 색상 구분은 이 값을 묶어서 정한다. 어떤 색으로 그릴지는 화면이 판단하므로
 * 서버는 유형만 내려준다.
 *
 * <p>{@link #PERSONAL} 만 훈련생이 만들 수 있고, 나머지는 운영진 전용이다.
 * 훈련생 등록 화면에 유형 선택이 없는 것도 그래서다.
 *
 * <p>{@link #HOLIDAY} 는 사람이 만들 수 없다. 시스템이 넣는 값이라
 * 등록 화면의 유형 목록에도 없다.
 */
public enum EventType {

    /** 수업. 화면의 '수업/발표' 이고, 발표와 과제 제출도 여기에 들어간다 */
    CLASS(false, false),

    /**
     * 발표.
     *
     * <p>화면이 수업과 같은 색으로 그리고 유형별 조회도 없어, 나눠 둔 값을 읽는 곳이 없었다.
     * 드롭다운을 '수업/발표' 하나로 합치면서 더 이상 새로 만들지 않는다.
     * 값을 지우지 않는 이유는 이미 저장된 일정을 읽지 못하게 되기 때문이다.
     */
    @Deprecated
    PRESENTATION(false, false),

    /** 과제 제출. {@link #PRESENTATION} 과 같은 이유로 더 이상 새로 만들지 않는다 */
    @Deprecated
    ASSIGNMENT(false, false),

    /** 행사 */
    EVENT(false, false),

    /** 개인 일정. 등록한 본인에게만 보인다 */
    PERSONAL(true, false),

    /**
     * 공휴일. 시스템이 넣고 등록자가 없어 아무도 지울 수 없다.
     *
     * <p>화면은 이 유형을 일정 칩이 아니라 날짜 표시로 그린다.
     */
    HOLIDAY(false, true);

    private final boolean personal;
    private final boolean systemOnly;

    EventType(boolean personal, boolean systemOnly) {
        this.personal = personal;
        this.systemOnly = systemOnly;
    }

    /**
     * 등록한 사람에게만 보이는 유형인지 여부.
     */
    public boolean isPersonal() {
        return personal;
    }

    /**
     * 사람이 직접 만들 수 없는 유형인지 여부.
     */
    public boolean isSystemOnly() {
        return systemOnly;
    }

    public static EventType from(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "일정 유형은 필수입니다."
            );
        }

        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "지원하지 않는 일정 유형입니다: " + value
                ));
    }
}

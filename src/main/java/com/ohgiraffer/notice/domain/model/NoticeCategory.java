package com.ohgiraffer.notice.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

/**
 * 공지 카테고리 도메인 모델. JPA와 무관한 순수 객체다.
 *
 * <p>화면의 '전체' 탭은 이 목록에 들어오지 않는다. 필터를 걸지 않는다는 뜻의 화면 라벨이지
 * 공지가 가질 수 있는 분류가 아니다. 실제 카테고리는 수업·과제·운영·지원처럼 공지에 붙는 것들뿐이다.
 *
 * <p>테이블에 is_default 컬럼이 남아 있으나 다루지 않는다. 작성 화면 드롭다운이
 * 미리 선택된 항목 없이 시작하는 것으로 정해져 쓸 데가 없어졌고, ERD 정리 대상이다.
 */
public class NoticeCategory {

    private static final int NAME_MAX_LENGTH = 50;

    private final Long id;
    private final String name;

    private NoticeCategory(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * 관리 화면에서 새로 추가하는 카테고리를 만든다.
     */
    public static NoticeCategory create(String name) {
        return new NoticeCategory(null, requireValidName(name));
    }

    public static NoticeCategory restore(Long id, String name) {
        return new NoticeCategory(id, name);
    }

    private static String requireValidName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "카테고리 이름은 필수입니다."
            );
        }

        String trimmed = name.trim();

        if (trimmed.length() > NAME_MAX_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "카테고리 이름은 " + NAME_MAX_LENGTH + "자를 넘을 수 없습니다."
            );
        }

        return trimmed;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

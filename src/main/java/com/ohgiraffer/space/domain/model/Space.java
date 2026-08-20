package com.ohgiraffer.space.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

public final class Space {

    private static final int MAX_NAME_LENGTH = 100;

    private final Long id;
    private final String name;
    private final int capacity;

    private Space(
            Long id,
            String name,
            int capacity
    ) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    public static Space create(
            String name,
            int capacity
    ) {
        validateName(name);
        validateCapacity(capacity);

        return new Space(
                null,
                name.trim(),
                capacity
        );
    }

    public static Space restore(
            Long id,
            String name,
            int capacity
    ) {
        if (id == null || id <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "공간 ID가 올바르지 않습니다."
            );
        }

        validateName(name);
        validateCapacity(capacity);

        return new Space(
                id,
                name.trim(),
                capacity
        );
    }

    private static void validateName(
            String name
    ) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "공간명은 필수입니다."
            );
        }

        if (name.trim().length() > MAX_NAME_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "공간명은 100자 이하여야 합니다."
            );
        }
    }

    private static void validateCapacity(
            int capacity
    ) {
        if (capacity < 1) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "최대 수용 인원은 1명 이상이어야 합니다."
            );
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }
}
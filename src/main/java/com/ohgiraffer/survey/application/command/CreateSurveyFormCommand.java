package com.ohgiraffer.survey.application.command;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.time.LocalDateTime;

public record CreateSurveyFormCommand(
        String title,
        LocalDateTime dueAt,
        Long createdBy
) {

    public CreateSurveyFormCommand {

        if (title == null || title.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 제목은 필수입니다."
            );
        }

        String normalizedTitle = title.trim();

        if (normalizedTitle.length() > 255) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 제목은 255자 이하로 입력해야 합니다."
            );
        }

        if (dueAt == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 응답 마감 일시는 필수입니다."
            );
        }

        if (createdBy == null || createdBy <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 생성자 ID가 올바르지 않습니다."
            );
        }

        /*
         * record의 compact constructor에서는
         * 매개변수 값을 다시 대입하면 최종 필드에 정규화된 값이 저장됩니다.
         */
        title = normalizedTitle;
    }
}
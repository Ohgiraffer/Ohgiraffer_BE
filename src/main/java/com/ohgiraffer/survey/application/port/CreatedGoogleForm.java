package com.ohgiraffer.survey.application.port;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

public record CreatedGoogleForm(
        String googleFormId
) {

    public CreatedGoogleForm {

        if (googleFormId == null
                || googleFormId.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Google Forms API가 Form ID를 반환하지 않았습니다."
            );
        }

        googleFormId = googleFormId.trim();
    }

    /*
     * 매니저가 Google Form 문항을 편집할 URL입니다.
     *
     * googleFormId로 언제든 다시 만들 수 있으므로
     * DB에는 별도로 저장하지 않습니다.
     */
    public String editUrl() {
        return "https://docs.google.com/forms/d/"
                + googleFormId
                + "/edit";
    }
}
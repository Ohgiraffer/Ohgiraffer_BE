package com.ohgiraffer.survey.infrastructure.google;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.survey.application.port.CreatedGoogleForm;
import com.ohgiraffer.survey.application.port.GoogleFormPort;
import org.springframework.stereotype.Component;

@Component
public class DisabledGoogleFormAdapter
        implements GoogleFormPort {

    @Override
    public CreatedGoogleForm createDraft(
            String title
    ) {
        throw new BusinessException(
                ErrorCode.GOOGLE_FORM_API_ERROR,
                "Google Forms 연동이 비활성화되어 있습니다."
        );
    }

    @Override
    public void delete(
            String googleFormId
    ) {
        throw new BusinessException(
                ErrorCode.GOOGLE_FORM_API_ERROR,
                "Google Forms 연동이 비활성화되어 있습니다."
        );
    }
}
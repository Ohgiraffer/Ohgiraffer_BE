package com.ohgiraffer.survey.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.domain.model.Role;
import org.springframework.stereotype.Component;

@Component
public class SurveyFormAccessValidator {

    public void validateStaffAuthority(
            Long requesterId,
            Role requesterRole
    ) {
        if (requesterId == null
                || requesterId <= 0
                || requesterRole == null) {
            throw new BusinessException(
                    ErrorCode.SURVEY_FORM_ACCESS_DENIED
            );
        }

        if (requesterRole != Role.MANAGER
                && requesterRole != Role.INSTRUCTOR) {
            throw new BusinessException(
                    ErrorCode.SURVEY_FORM_ACCESS_DENIED
            );
        }
    }
}
package com.ohgiraffer.survey.infrastructure.google;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.survey.application.port.CreatedGoogleForm;
import com.ohgiraffer.survey.application.port.GoogleFormPort;
import org.springframework.stereotype.Component;
import com.ohgiraffer.survey.application.port.GoogleFormResponseInfo;

import java.util.List;

@Component
public class DisabledGoogleFormAdapter implements GoogleFormPort {

    @Override
    public CreatedGoogleForm createDraft(String title) {
        throw disabledException();
    }

    @Override
    public void enableVerifiedEmailCollection(String googleFormId) {
        throw disabledException();
    }

    @Override
    public void updatePublishState(String googleFormId, boolean published, boolean acceptingResponses) {
        throw disabledException();
    }

    @Override
    public boolean moveToTrash(String googleFormId) {
        throw disabledException();
    }

    @Override
    public void restoreFromTrash(String googleFormId) {
        throw disabledException();
    }

    @Override
    public void delete(String googleFormId) {
        throw disabledException();
    }

    private BusinessException disabledException() {
        return new BusinessException(
                ErrorCode.GOOGLE_FORM_API_ERROR,
                "Google Forms 연동이 비활성화되어 있습니다."
        );
    }

    @Override
    public List<GoogleFormResponseInfo> getResponses(String googleFormId) {
        throw disabledException();
    }
}
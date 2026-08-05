package com.ohgiraffer.survey.infrastructure.google;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.forms.v1.Forms;
import com.google.api.services.forms.v1.model.Form;
import com.google.api.services.forms.v1.model.Info;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.survey.application.port.CreatedGoogleForm;
import com.ohgiraffer.survey.application.port.GoogleFormPort;
import com.google.api.services.forms.v1.model.PublishSettings;
import com.google.api.services.forms.v1.model.PublishState;
import com.google.api.services.forms.v1.model.SetPublishSettingsRequest;

import java.util.List;
import java.io.IOException;

public class GoogleFormsAdapter implements GoogleFormPort {

    private final Forms forms;
    private final Drive drive;

    public GoogleFormsAdapter(
            Forms forms,
            Drive drive
    ) {
        this.forms = forms;
        this.drive = drive;
    }

    @Override
    public CreatedGoogleForm createDraft(
            String title
    ) {
        validateTitle(title);

        String normalizedTitle =
                title.trim();

        Form requestedForm =
                new Form()
                        .setInfo(
                                new Info()
                                        .setTitle(
                                                normalizedTitle
                                        )
                                        .setDocumentTitle(
                                                normalizedTitle
                                        )
                        );

        try {
            Form createdForm =
                    forms
                            .forms()
                            .create(
                                    requestedForm
                            )
                            .setUnpublished(true)
                            .execute();

            if (createdForm == null
                    || createdForm.getFormId() == null
                    || createdForm.getFormId().isBlank()) {
                throw new BusinessException(
                        ErrorCode.GOOGLE_FORM_API_ERROR,
                        "Google Forms API가 Form ID를 반환하지 않았습니다."
                );
            }

            return new CreatedGoogleForm(
                    createdForm.getFormId()
            );

        } catch (GoogleJsonResponseException exception) {
            throw convertGoogleException(
                    exception
            );

        } catch (IOException exception) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_FORM_API_ERROR
            );
        }
    }

    @Override
    public void updatePublishState(
            String googleFormId,
            boolean published,
            boolean acceptingResponses
    ) {
        validateGoogleFormId(googleFormId);

        PublishState publishState =
                new PublishState()
                        .setIsPublished(published)
                        .setIsAcceptingResponses(
                                acceptingResponses
                        );

        PublishSettings publishSettings =
                new PublishSettings()
                        .setPublishState(
                                publishState
                        );

        SetPublishSettingsRequest request =
                new SetPublishSettingsRequest()
                        .setPublishSettings(
                                publishSettings
                        )
                        .setUpdateMask("publishState");

        try {
            forms
                    .forms()
                    .setPublishSettings(
                            googleFormId.trim(),
                            request
                    )
                    .execute();

        } catch (GoogleJsonResponseException exception) {
            throw convertGoogleException(exception);

        } catch (IOException exception) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_FORM_API_ERROR
            );
        }
    }

    @Override
    public void delete(
            String googleFormId
    ) {
        validateGoogleFormId(
                googleFormId
        );

        try {
            /*
             * Google Form은 Google Drive 파일이므로
             * 삭제는 Forms API가 아니라 Drive API를 사용합니다.
             */
            drive
                    .files()
                    .delete(
                            googleFormId.trim()
                    )
                    .execute();

        } catch (GoogleJsonResponseException exception) {
            throw convertGoogleException(
                    exception
            );

        } catch (IOException exception) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_FORM_API_ERROR
            );
        }
    }

    private void validateTitle(
            String title
    ) {
        if (title == null || title.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 제목은 필수입니다."
            );
        }

        if (title.trim().length() > 255) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 제목은 255자 이하로 입력해야 합니다."
            );
        }
    }

    private void validateGoogleFormId(
            String googleFormId
    ) {
        if (googleFormId == null
                || googleFormId.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "Google Form ID는 필수입니다."
            );
        }
    }

    private BusinessException convertGoogleException(
            GoogleJsonResponseException exception
    ) {
        return switch (exception.getStatusCode()) {
            case 401, 403 -> new BusinessException(
                    ErrorCode.GOOGLE_FORM_ACCESS_DENIED
            );

            case 404 -> new BusinessException(
                    ErrorCode.GOOGLE_FORM_NOT_FOUND
            );

            case 429 -> new BusinessException(
                    ErrorCode.GOOGLE_FORM_RATE_LIMIT_EXCEEDED
            );

            default -> new BusinessException(
                    ErrorCode.GOOGLE_FORM_API_ERROR
            );
        };
    }
}
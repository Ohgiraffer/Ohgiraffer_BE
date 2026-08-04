package com.ohgiraffer.survey.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.survey.application.usecase.GetSurveyFormDetailUseCase;
import com.ohgiraffer.survey.application.usecase.GetSurveyFormListUseCase;
import com.ohgiraffer.survey.application.usecase.SurveyFormDetailResult;
import com.ohgiraffer.survey.application.usecase.SurveyFormListResult;
import com.ohgiraffer.survey.domain.model.SurveyForm;
import com.ohgiraffer.survey.domain.repository.SurveyFormRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class QuerySurveyFormService
        implements GetSurveyFormListUseCase,
        GetSurveyFormDetailUseCase {

    private final SurveyFormRepository surveyFormRepository;

    public QuerySurveyFormService(
            SurveyFormRepository surveyFormRepository
    ) {
        this.surveyFormRepository = surveyFormRepository;
    }

    @Override
    public List<SurveyFormListResult> getSurveyForms() {
        return surveyFormRepository
                .findAll()
                .stream()
                .map(SurveyFormListResult::from)
                .toList();
    }

    @Override
    public SurveyFormDetailResult getSurveyForm(
            Long surveyFormId
    ) {
        validateSurveyFormId(surveyFormId);

        SurveyForm surveyForm =
                surveyFormRepository
                        .findById(surveyFormId)
                        .orElseThrow(
                                () -> new BusinessException(
                                        ErrorCode.SURVEY_FORM_NOT_FOUND
                                )
                        );

        return SurveyFormDetailResult.from(
                surveyForm
        );
    }

    private void validateSurveyFormId(
            Long surveyFormId
    ) {
        if (surveyFormId == null
                || surveyFormId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 폼 ID가 올바르지 않습니다."
            );
        }
    }
}
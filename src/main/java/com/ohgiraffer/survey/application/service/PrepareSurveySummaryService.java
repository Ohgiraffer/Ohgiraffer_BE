package com.ohgiraffer.survey.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.survey.application.port.SurveyResponseDataPort;
import com.ohgiraffer.survey.application.port.SurveyResponseDataset;
import com.ohgiraffer.survey.application.summary.SurveyStatisticsCalculator;
import com.ohgiraffer.survey.application.summary.SurveyStatisticsResult;
import com.ohgiraffer.survey.application.summary.SurveySummaryFileNameGenerator;
import com.ohgiraffer.survey.application.usecase.PrepareSurveySummaryUseCase;
import com.ohgiraffer.survey.application.usecase.SurveySummaryPreparationResult;
import com.ohgiraffer.survey.domain.model.SurveyForm;
import com.ohgiraffer.survey.domain.model.sheet.SurveySheetLink;
import com.ohgiraffer.survey.domain.repository.SurveyFormRepository;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.survey.application.port.SurveySummaryAiPort;
import com.ohgiraffer.survey.application.summary.SurveyAiSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.time.Instant;

@Service
public class PrepareSurveySummaryService
        implements PrepareSurveySummaryUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(
                    PrepareSurveySummaryService.class
            );

    private final SurveyFormRepository surveyFormRepository;
    private final SurveyFormAccessValidator accessValidator;
    private final SurveySheetLinkPersistenceService surveySheetLinkPersistenceService;
    private final SurveyResponseDataPort surveyResponseDataPort;
    private final SurveyStatisticsCalculator statisticsCalculator;
    private final SurveySummaryFileNameGenerator fileNameGenerator;
    private final SurveySummaryAiPort surveySummaryAiPort;

    public PrepareSurveySummaryService(
            SurveyFormRepository surveyFormRepository,
            SurveyFormAccessValidator accessValidator,
            SurveySheetLinkPersistenceService
                    surveySheetLinkPersistenceService,
            SurveyResponseDataPort surveyResponseDataPort,
            SurveyStatisticsCalculator statisticsCalculator,
            SurveySummaryFileNameGenerator fileNameGenerator,
            SurveySummaryAiPort surveySummaryAiPort
    ) {
        this.surveyFormRepository = surveyFormRepository;
        this.accessValidator = accessValidator;
        this.surveySheetLinkPersistenceService = surveySheetLinkPersistenceService;
        this.surveyResponseDataPort = surveyResponseDataPort;
        this.statisticsCalculator = statisticsCalculator;
        this.fileNameGenerator = fileNameGenerator;
        this.surveySummaryAiPort = surveySummaryAiPort;
    }

    @Override
    public SurveySummaryPreparationResult prepare(
            Long surveyFormId,
            Long requesterId,
            Role requesterRole
    ) {
        log.info(
                "설문 요약 준비 시작. surveyFormId={}",
                surveyFormId
        );

        accessValidator.validateStaffAuthority(
                requesterId,
                requesterRole
        );

        validateSurveyFormId(
                surveyFormId
        );

        SurveyForm surveyForm =
                findSurveyForm(
                        surveyFormId
                );

        log.debug(
                "설문 폼 조회 완료. surveyFormId={}",
                surveyFormId
        );

        SurveySheetLink sheetLink =
                findSheetLink(
                        surveyFormId
                );

        log.debug(
                "설문 시트 연결 조회 완료. "
                        + "surveyFormId={}, sheetName={}",
                surveyFormId,
                sheetLink.getSheetName()
        );

        SurveyResponseDataset dataset =
                surveyResponseDataPort.readResponses(
                        sheetLink.getSpreadsheetId(),
                        sheetLink.getSpreadsheetTitle(),
                        sheetLink.getSheetName()
                );

        validateDataset(
                dataset
        );

        log.info(
                "설문 응답 조회 완료. "
                        + "surveyFormId={}, responseCount={}, columnCount={}",
                surveyFormId,
                dataset.responseCount(),
                dataset.headers().size()
        );

        SurveyStatisticsResult statistics =
                statisticsCalculator.calculate(
                        dataset
                );

        validateStatistics(
                statistics
        );

        log.info(
                "설문 통계 계산 완료. "
                        + "surveyFormId={}, questionCount={}",
                surveyFormId,
                statistics.totalQuestionCount()
        );

        SurveyAiSummary aiSummary =
                generateAiSummary(
                        surveyForm.getTitle(),
                        statistics
                );

        log.info(
                "설문 AI 분석 완료. "
                        + "surveyFormId={}, aiGenerated={}",
                surveyFormId,
                aiSummary.generated()
        );

        String fileName =
                fileNameGenerator.generate(
                        surveyForm.getTitle()
                );

        SurveySummaryPreparationResult result =
                new SurveySummaryPreparationResult(
                        surveyForm.getId(),
                        surveyForm.getTitle(),
                        Instant.now(),
                        fileName,
                        statistics,
                        aiSummary
                );

        log.info(
                "설문 요약 준비 완료. surveyFormId={}",
                surveyFormId
        );

        return result;
    }

    private SurveyForm findSurveyForm(
            Long surveyFormId
    ) {
        return surveyFormRepository.findById(
                        surveyFormId
                )
                .orElseThrow(
                        () -> new BusinessException(
                                ErrorCode.SURVEY_FORM_NOT_FOUND
                        )
                );
    }

    private SurveySheetLink findSheetLink(
            Long surveyFormId
    ) {
        return surveySheetLinkPersistenceService
                .findBySurveyFormId(
                        surveyFormId
                )
                .orElseThrow(
                        () -> new BusinessException(
                                ErrorCode
                                        .SURVEY_SHEET_LINK_NOT_FOUND
                        )
                );
    }

    private void validateDataset(
            SurveyResponseDataset dataset
    ) {
        if (dataset == null || dataset.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.SURVEY_RESPONSE_NOT_FOUND
            );
        }
    }

    private void validateStatistics(
            SurveyStatisticsResult statistics
    ) {
        if (statistics == null
                || !statistics.hasResponses()
                || !statistics.hasQuestions()) {
            throw new BusinessException(
                    ErrorCode.SURVEY_RESPONSE_NOT_FOUND
            );
        }
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

    private SurveyAiSummary generateAiSummary(
            String surveyTitle,
            SurveyStatisticsResult statistics
    ) {
        try {
            return surveySummaryAiPort.summarize(
                    surveyTitle,
                    statistics
            );
        } catch (BusinessException exception) {
            if (exception.getErrorCode()
                    != ErrorCode.AI_API_CALL_FAILED) {
                throw exception;
            }

            log.warn(
                    "Gemini 설문 요약 생성에 실패하여 "
                            + "기본 통계만 사용합니다. "
                            + "surveyTitle={}",
                    surveyTitle,
                    exception
            );

            return SurveyAiSummary.unavailable();
        }
    }
}
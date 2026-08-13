package com.ohgiraffer.survey.application.service;

import com.ohgiraffer.survey.application.port.SurveySummaryPdfPort;
import com.ohgiraffer.survey.application.usecase.GenerateSurveySummaryPdfUseCase;
import com.ohgiraffer.survey.application.usecase.PrepareSurveySummaryUseCase;
import com.ohgiraffer.survey.application.usecase.SurveySummaryPdfResult;
import com.ohgiraffer.survey.application.usecase.SurveySummaryPreparationResult;
import com.ohgiraffer.user.domain.model.Role;
import org.springframework.stereotype.Service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GenerateSurveySummaryPdfService implements GenerateSurveySummaryPdfUseCase {

    private final PrepareSurveySummaryUseCase prepareSurveySummaryUseCase;
    private final SurveySummaryPdfPort surveySummaryPdfPort;
    private static final Logger log =
            LoggerFactory.getLogger(
                    GenerateSurveySummaryPdfService.class
            );

    public GenerateSurveySummaryPdfService(
            PrepareSurveySummaryUseCase
                    prepareSurveySummaryUseCase,
            SurveySummaryPdfPort surveySummaryPdfPort
    ) {
        this.prepareSurveySummaryUseCase =
                prepareSurveySummaryUseCase;

        this.surveySummaryPdfPort =
                surveySummaryPdfPort;
    }

    @Override
    public SurveySummaryPdfResult generate(
            Long surveyFormId,
            Long requesterId,
            Role requesterRole
    ) {
        String stage = "PREPARE_SUMMARY";

        try {
            SurveySummaryPreparationResult preparation =
                    prepareSurveySummaryUseCase.prepare(
                            surveyFormId,
                            requesterId,
                            requesterRole
                    );

            stage = "GENERATE_PDF";

            byte[] pdfContent =
                    surveySummaryPdfPort.generate(
                            preparation
                    );

            if (pdfContent == null
                    || pdfContent.length == 0) {
                throw new BusinessException(
                        ErrorCode.SURVEY_PDF_GENERATION_FAILED
                );
            }

            stage = "CREATE_RESULT";

            return new SurveySummaryPdfResult(
                    preparation.fileName(),
                    pdfContent
            );

        } catch (BusinessException exception) {
            log.error(
                    "설문 요약 PDF 요청 실패. "
                            + "surveyFormId={}, stage={}, errorCode={}",
                    surveyFormId,
                    stage,
                    exception.getErrorCode(),
                    exception
            );

            throw exception;

        } catch (RuntimeException exception) {
            log.error(
                    "설문 요약 PDF 요청 중 예상하지 못한 오류. "
                            + "surveyFormId={}, stage={}",
                    surveyFormId,
                    stage,
                    exception
            );

            throw new BusinessException(
                    ErrorCode.SURVEY_PDF_GENERATION_FAILED,
                    exception
            );
        }
    }

}
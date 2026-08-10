package com.ohgiraffer.survey.application.service;

import com.ohgiraffer.survey.application.port.SurveySummaryPdfPort;
import com.ohgiraffer.survey.application.usecase.GenerateSurveySummaryPdfUseCase;
import com.ohgiraffer.survey.application.usecase.PrepareSurveySummaryUseCase;
import com.ohgiraffer.survey.application.usecase.SurveySummaryPdfResult;
import com.ohgiraffer.survey.application.usecase.SurveySummaryPreparationResult;
import com.ohgiraffer.user.domain.model.Role;
import org.springframework.stereotype.Service;

@Service
public class GenerateSurveySummaryPdfService
        implements GenerateSurveySummaryPdfUseCase {

    private final PrepareSurveySummaryUseCase
            prepareSurveySummaryUseCase;

    private final SurveySummaryPdfPort
            surveySummaryPdfPort;

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
        SurveySummaryPreparationResult preparation =
                prepareSurveySummaryUseCase.prepare(
                        surveyFormId,
                        requesterId,
                        requesterRole
                );

        byte[] pdfContent =
                surveySummaryPdfPort.generate(
                        preparation
                );

        return new SurveySummaryPdfResult(
                preparation.fileName(),
                pdfContent
        );
    }
}
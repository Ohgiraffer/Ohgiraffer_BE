package com.ohgiraffer.survey.infrastructure.pdf;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.survey.application.port.SurveySummaryPdfPort;
import com.ohgiraffer.survey.application.usecase.SurveySummaryPreparationResult;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class OpenHtmlSurveySummaryPdfAdapter
        implements SurveySummaryPdfPort {

    private static final String TEMPLATE_NAME = "pdf/survey-summary";
    private static final String REGULAR_FONT_PATH = "fonts/NotoSansKR-Regular.ttf";
    private static final String MEDIUM_FONT_PATH = "fonts/NotoSansKR-Medium.ttf";
    private static final String BOLD_FONT_PATH = "fonts/NotoSansKR-Bold.ttf";
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter GENERATED_AT_FORMATTER = DateTimeFormatter.ofPattern(
                            "yyyy년 MM월 dd일 HH:mm").withZone(KOREA_ZONE_ID);
    private final TemplateEngine templateEngine;
    private static final Logger log = LoggerFactory.getLogger(OpenHtmlSurveySummaryPdfAdapter.class);

    public OpenHtmlSurveySummaryPdfAdapter(
            TemplateEngine templateEngine
    ) {
        this.templateEngine = templateEngine;
    }

    @Override
    public byte[] generate(
            SurveySummaryPreparationResult result
    ) {
        Long surveyFormId =
                result == null
                        ? null
                        : result.surveyFormId();

        String stage = "VALIDATE_RESULT";

        try (
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {
            validateResult(result);

            stage = "CREATE_TEMPLATE_CONTEXT";

            Context context =
                    createContext(result);

            stage = "RENDER_THYMELEAF_TEMPLATE";

            String html =
                    templateEngine.process(
                            TEMPLATE_NAME,
                            context
                    );

            if (html == null || html.isBlank()) {
                throw new BusinessException(
                        ErrorCode.SURVEY_PDF_GENERATION_FAILED
                );
            }

            stage = "CONFIGURE_PDF_RENDERER";

            PdfRendererBuilder builder =
                    new PdfRendererBuilder();

            builder.useFastMode();

            builder.withHtmlContent(
                    html,
                    getBaseUri()
            );

            builder.useFont(
                    this::openRegularFont,
                    "Noto Sans KR",
                    400,
                    PdfRendererBuilder.FontStyle.NORMAL,
                    true
            );

            builder.useFont(
                    this::openMediumFont,
                    "Noto Sans KR",
                    500,
                    PdfRendererBuilder.FontStyle.NORMAL,
                    true
            );

            builder.useFont(
                    this::openBoldFont,
                    "Noto Sans KR",
                    700,
                    PdfRendererBuilder.FontStyle.NORMAL,
                    true
            );

            stage = "GENERATE_PDF";

            builder.toStream(outputStream);
            builder.run();

            byte[] pdfBytes =
                    outputStream.toByteArray();

            if (pdfBytes.length == 0) {
                throw new BusinessException(
                        ErrorCode.SURVEY_PDF_GENERATION_FAILED
                );
            }

            log.info(
                    "설문 요약 PDF 생성 완료. surveyFormId={}, pdfSize={}",
                    surveyFormId,
                    pdfBytes.length
            );

            return pdfBytes;

        } catch (BusinessException exception) {
            log.error(
                    "설문 요약 PDF 생성 실패. surveyFormId={}, stage={}, errorCode={}",
                    surveyFormId,
                    stage,
                    exception.getErrorCode(),
                    exception
            );

            throw exception;

        } catch (Exception exception) {
            log.error(
                    "설문 요약 PDF 생성 중 예상하지 못한 오류. "
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

    private Context createContext(
            SurveySummaryPreparationResult result
    ) {
        Context context =
                new Context();

        context.setVariable(
                "surveyFormId",
                result.surveyFormId()
        );

        context.setVariable(
                "surveyTitle",
                result.surveyTitle()
        );

        context.setVariable(
                "generatedAt",
                GENERATED_AT_FORMATTER.format(
                        result.generatedAt()
                )
        );

        context.setVariable(
                "statistics",
                result.statistics()
        );

        context.setVariable(
                "questions",
                result.statistics()
                        .questions()
        );

        context.setVariable(
                "aiSummary",
                result.aiSummary()
        );

        return context;
    }

    private void validateResult(
            SurveySummaryPreparationResult result
    ) {
        if (result == null
                || result.statistics() == null
                || result.aiSummary() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private String getBaseUri() {
        try {
            return new ClassPathResource(
                    "templates/pdf/"
            )
                    .getURL()
                    .toExternalForm();
        } catch (IOException exception) {
            throw new BusinessException(
                    ErrorCode
                            .SURVEY_PDF_GENERATION_FAILED,
                    exception
            );
        }
    }

    private InputStream openRegularFont() {
        return openFont(
                REGULAR_FONT_PATH
        );
    }

    private InputStream openMediumFont() {
        return openFont(
                MEDIUM_FONT_PATH
        );
    }

    private InputStream openBoldFont() {
        return openFont(
                BOLD_FONT_PATH
        );
    }

    private InputStream openFont(
            String path
    ) {
        try {
            return new ClassPathResource(
                    path
            )
                    .getInputStream();
        } catch (IOException exception) {
            throw new BusinessException(
                    ErrorCode
                            .SURVEY_PDF_GENERATION_FAILED,
                    exception
            );
        }
    }
}
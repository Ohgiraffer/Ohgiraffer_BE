package com.ohgiraffer.approval.infrastructure.pdf;

import com.ohgiraffer.approval.application.port.GenerateApprovalPdfPort;
import com.ohgiraffer.approval.application.query.LeavePdfData;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class OpenHtmlApprovalPdfAdapter implements GenerateApprovalPdfPort {

    private static final String LEAVE_APPLICATION_TEMPLATE =
            "pdf/leave-application";

    private static final String REGULAR_FONT_PATH =
            "fonts/NotoSansKR-Regular.ttf";

    private static final String MEDIUM_FONT_PATH =
            "fonts/NotoSansKR-Medium.ttf";

    private static final String BOLD_FONT_PATH =
            "fonts/NotoSansKR-Bold.ttf";

    private final TemplateEngine templateEngine;

    @Override
    public byte[] generateLeaveApplicationPdf(
            LeavePdfData data
    ) {
        validateData(
                data
        );

        String html =
                templateEngine.process(
                        LEAVE_APPLICATION_TEMPLATE,
                        createContext(
                                data
                        )
                );

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
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
            builder.toStream(
                    outputStream
            );
            builder.run();

            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    exception
            );
        }
    }

    private void validateData(
            LeavePdfData data
    ) {
        if (data == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private Context createContext(
            LeavePdfData data
    ) {
        Context context =
                new Context();

        context.setVariable(
                "approvalId",
                data.approvalId()
        );
        context.setVariable(
                "studentName",
                data.studentName()
        );
        context.setVariable(
                "courseName",
                data.courseName()
        );
        context.setVariable(
                "leaveStartDate",
                data.leaveStartDate()
        );
        context.setVariable(
                "leaveEndDate",
                data.leaveEndDate()
        );
        context.setVariable(
                "leaveDays",
                data.leaveDays()
        );
        context.setVariable(
                "requestedDate",
                data.requestedDate()
        );
        context.setVariable(
                "approverName",
                data.approverName()
        );
        context.setVariable(
                "requesterSignatureImage",
                data.requesterSignatureImage()
        );
        context.setVariable(
                "approverSignatureImage",
                data.approverSignatureImage()
        );

        return context;
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
                    ErrorCode.INTERNAL_SERVER_ERROR,
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
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    exception
            );
        }
    }
}
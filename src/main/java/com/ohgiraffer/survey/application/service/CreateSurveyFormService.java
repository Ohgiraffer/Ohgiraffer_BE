package com.ohgiraffer.survey.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.survey.application.command.CreateSurveyFormCommand;
import com.ohgiraffer.survey.application.port.CreatedGoogleForm;
import com.ohgiraffer.survey.application.port.GoogleFormPort;
import com.ohgiraffer.survey.application.usecase.CreateSurveyFormResult;
import com.ohgiraffer.survey.application.usecase.CreateSurveyFormUseCase;
import com.ohgiraffer.survey.domain.model.SurveyForm;
import com.ohgiraffer.survey.domain.repository.SurveyFormRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class CreateSurveyFormService
        implements CreateSurveyFormUseCase {

    private final GoogleFormPort googleFormPort;
    private final SurveyFormRepository surveyFormRepository;
    private final Clock clock;

    public CreateSurveyFormService(
            GoogleFormPort googleFormPort,
            SurveyFormRepository surveyFormRepository,
            Clock clock
    ) {
        this.googleFormPort = googleFormPort;
        this.surveyFormRepository = surveyFormRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CreateSurveyFormResult create(
            CreateSurveyFormCommand command
    ) {
        validateDueAt(
                command.dueAt()
        );

        /*
         * 1. Google Forms API를 호출하여
         * 아직 게시되지 않은 빈 Form을 생성합니다.
         */
        CreatedGoogleForm createdGoogleForm =
                googleFormPort.createDraft(
                        command.title()
                );

        try {
            /*
             * 2. Google에서 반환받은 Form ID를 이용하여
             * 설문 도메인 객체를 생성합니다.
             */
            SurveyForm surveyForm =
                    SurveyForm.create(
                            command.title(),
                            command.dueAt(),
                            createdGoogleForm.googleFormId(),
                            command.createdBy()
                    );

            /*
             * 3. DB에 저장합니다.
             *
             * saveAndFlush를 사용하므로 이 위치에서
             * INSERT 오류를 확인할 수 있습니다.
             */
            SurveyForm savedSurveyForm =
                    surveyFormRepository.save(
                            surveyForm
                    );

            /*
             * 4. Controller에 전달할 결과를 반환합니다.
             */
            return CreateSurveyFormResult.from(
                    savedSurveyForm
            );

        } catch (RuntimeException saveException) {
            /*
             * Google Form 생성 후 DB 저장이 실패하면
             * 외부 Google Drive에 고아 Form이 남지 않도록 삭제합니다.
             */
            compensateGoogleFormCreation(
                    createdGoogleForm.googleFormId(),
                    saveException
            );

            throw saveException;
        }
    }

    private void validateDueAt(
            LocalDateTime dueAt
    ) {
        LocalDateTime now =
                LocalDateTime.now(
                        clock
                );

        if (!dueAt.isAfter(now)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 응답 마감 일시는 현재 시간 이후여야 합니다."
            );
        }
    }

    private void compensateGoogleFormCreation(
            String googleFormId,
            RuntimeException originalException
    ) {
        try {
            googleFormPort.delete(
                    googleFormId
            );

        } catch (RuntimeException compensationException) {
            /*
             * 보상 삭제 실패가 원래 DB 오류를 덮어쓰지 않도록
             * suppressed exception으로 기록합니다.
             */
            originalException.addSuppressed(
                    compensationException
            );
        }
    }
}
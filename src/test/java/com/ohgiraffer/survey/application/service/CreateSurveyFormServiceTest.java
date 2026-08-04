package com.ohgiraffer.survey.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.survey.application.command.CreateSurveyFormCommand;
import com.ohgiraffer.survey.application.port.CreatedGoogleForm;
import com.ohgiraffer.survey.application.port.GoogleFormPort;
import com.ohgiraffer.survey.application.usecase.CreateSurveyFormResult;
import com.ohgiraffer.survey.domain.model.SurveyForm;
import com.ohgiraffer.survey.domain.model.SurveyFormStatus;
import com.ohgiraffer.survey.domain.repository.SurveyFormRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSurveyFormServiceTest {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final Instant FIXED_INSTANT =
            Instant.parse("2026-08-04T00:00:00Z");

    @Mock
    private GoogleFormPort googleFormPort;

    @Mock
    private SurveyFormRepository surveyFormRepository;

    private CreateSurveyFormService createSurveyFormService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(FIXED_INSTANT, SEOUL_ZONE);

        createSurveyFormService = new CreateSurveyFormService(
                googleFormPort,
                surveyFormRepository,
                fixedClock
        );
    }

    @Test
    @DisplayName("Google 폼을 생성하고 설문 정보를 DB에 저장한다")
    void createSurveyForm() {
        // given
        String title = "7월 동료 평가";
        LocalDateTime dueAt =
                LocalDateTime.of(2026, 8, 10, 23, 59);
        Long createdBy = 1L;
        String googleFormId = "google-form-id";

        CreateSurveyFormCommand command =
                new CreateSurveyFormCommand(title, dueAt, createdBy);

        CreatedGoogleForm createdGoogleForm =
                new CreatedGoogleForm(googleFormId);

        SurveyForm savedSurveyForm = SurveyForm.restore(
                1L,
                title,
                dueAt,
                SurveyFormStatus.DRAFT,
                googleFormId,
                createdBy,
                Instant.parse("2026-08-04T00:01:00Z"),
                Instant.parse("2026-08-04T00:01:00Z")
        );

        when(googleFormPort.createDraft(title))
                .thenReturn(createdGoogleForm);

        when(surveyFormRepository.save(any(SurveyForm.class)))
                .thenReturn(savedSurveyForm);

        // when
        CreateSurveyFormResult result =
                createSurveyFormService.create(command);

        // then
        assertEquals(1L, result.surveyFormId());
        assertEquals(title, result.title());
        assertEquals(dueAt, result.dueAt());
        assertEquals(SurveyFormStatus.DRAFT, result.status());
        assertEquals(googleFormId, result.googleFormId());
        assertEquals(
                "https://docs.google.com/forms/d/google-form-id/edit",
                result.editUrl()
        );

        verify(googleFormPort).createDraft(title);
        verify(surveyFormRepository).save(any(SurveyForm.class));
        verify(googleFormPort, never()).delete(any());
    }

    @Test
    @DisplayName("응답 마감일이 현재 시각 이전이면 설문을 생성하지 않는다")
    void rejectPastDueAt() {
        // given
        LocalDateTime pastDueAt =
                LocalDateTime.of(2026, 8, 4, 8, 59);

        CreateSurveyFormCommand command =
                new CreateSurveyFormCommand(
                        "마감된 설문",
                        pastDueAt,
                        1L
                );

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> createSurveyFormService.create(command)
        );

        // then
        assertTrue(exception.getMessage().contains("마감"));

        verifyNoInteractions(googleFormPort);
        verifyNoInteractions(surveyFormRepository);
    }

    @Test
    @DisplayName("DB 저장에 실패하면 생성된 Google 폼을 삭제한다")
    void deleteGoogleFormWhenDatabaseSaveFails() {
        // given
        String title = "DB 저장 실패 설문";
        String googleFormId = "orphan-google-form-id";

        CreateSurveyFormCommand command =
                new CreateSurveyFormCommand(
                        title,
                        LocalDateTime.of(2026, 8, 10, 23, 59),
                        1L
                );

        RuntimeException databaseException =
                new RuntimeException("DB 저장 실패");

        when(googleFormPort.createDraft(title))
                .thenReturn(new CreatedGoogleForm(googleFormId));

        when(surveyFormRepository.save(any(SurveyForm.class)))
                .thenThrow(databaseException);

        // when
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> createSurveyFormService.create(command)
        );

        // then
        assertEquals(databaseException, thrown);

        verify(googleFormPort).createDraft(title);
        verify(surveyFormRepository).save(any(SurveyForm.class));
        verify(googleFormPort).delete(googleFormId);
    }

    @Test
    @DisplayName("보상 삭제에도 실패하면 원래 예외에 suppressed 예외를 추가한다")
    void preserveOriginalExceptionWhenCompensationFails() {
        // given
        String title = "보상 삭제 실패 설문";
        String googleFormId = "cleanup-failure-form-id";

        CreateSurveyFormCommand command =
                new CreateSurveyFormCommand(
                        title,
                        LocalDateTime.of(2026, 8, 10, 23, 59),
                        1L
                );

        RuntimeException databaseException =
                new RuntimeException("DB 저장 실패");

        RuntimeException cleanupException =
                new RuntimeException("Google 폼 삭제 실패");

        when(googleFormPort.createDraft(title))
                .thenReturn(new CreatedGoogleForm(googleFormId));

        when(surveyFormRepository.save(any(SurveyForm.class)))
                .thenThrow(databaseException);

        doThrow(cleanupException)
                .when(googleFormPort)
                .delete(googleFormId);

        // when
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> createSurveyFormService.create(command)
        );

        // then
        assertEquals(databaseException, thrown);
        assertEquals(1, thrown.getSuppressed().length);
        assertEquals(cleanupException, thrown.getSuppressed()[0]);

        verify(googleFormPort).delete(googleFormId);
    }

    @Test
    @DisplayName("Google 폼 생성에 실패하면 DB 저장과 보상 삭제를 실행하지 않는다")
    void doNotSaveWhenGoogleFormCreationFails() {
        // given
        String title = "Google API 실패 설문";

        CreateSurveyFormCommand command =
                new CreateSurveyFormCommand(
                        title,
                        LocalDateTime.of(2026, 8, 10, 23, 59),
                        1L
                );

        BusinessException googleApiException =
                new BusinessException(
                        com.ohgiraffer.global.exception.ErrorCode
                                .GOOGLE_FORM_API_ERROR
                );

        when(googleFormPort.createDraft(title))
                .thenThrow(googleApiException);

        // when
        BusinessException thrown = assertThrows(
                BusinessException.class,
                () -> createSurveyFormService.create(command)
        );

        // then
        assertEquals(googleApiException, thrown);

        verify(googleFormPort).createDraft(title);
        verify(googleFormPort, never()).delete(any());
        verifyNoInteractions(surveyFormRepository);
    }
}
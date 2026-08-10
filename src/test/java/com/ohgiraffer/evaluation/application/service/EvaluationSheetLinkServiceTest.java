package com.ohgiraffer.evaluation.application.service;

import com.ohgiraffer.evaluation.application.command.SaveEvaluationSheetLinkCommand;
import com.ohgiraffer.evaluation.domain.model.EvaluationColumnMapping;
import com.ohgiraffer.evaluation.domain.model.EvaluationSheetLink;
import com.ohgiraffer.evaluation.domain.repository.EvaluationSheetLinkRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.google.sheets.ExternalSheetValidationResult;
import com.ohgiraffer.global.google.sheets.SheetColumn;
import com.ohgiraffer.global.google.sheets.ValidateExternalSheetUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 저장 전에 시트를 열어 탭과 컬럼을 대조하는지, 이미 있는 연동을 덮어쓰는지 확인한다.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EvaluationSheetLinkServiceTest {

    private static final String SHEET_URL =
            "https://docs.google.com/spreadsheets/d/1AbCdEf123/edit";

    private static final List<String> SHEET_COLUMNS =
            List.of("이메일", "이름", "평가유형", "평가항목", "점수", "의견");

    @Mock
    private EvaluationSheetLinkRepository evaluationSheetLinkRepository;

    @Mock
    private ValidateExternalSheetUseCase validateExternalSheetUseCase;

    private EvaluationSheetLinkService evaluationSheetLinkService;

    @BeforeEach
    void setUp() {
        evaluationSheetLinkService = new EvaluationSheetLinkService(
                evaluationSheetLinkRepository,
                validateExternalSheetUseCase
        );

        when(validateExternalSheetUseCase.validate(any()))
                .thenReturn(new ExternalSheetValidationResult(
                        "1AbCdEf123",
                        "CampFlow 평가 데이터",
                        List.of(new SheetColumn("시트1", SHEET_COLUMNS))
                ));

        when(evaluationSheetLinkRepository.find())
                .thenReturn(Optional.empty());
        when(evaluationSheetLinkRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("탭을 지정하지 않으면 첫 번째 탭을 쓴다")
    void saveUsesFirstSheetWhenTabNotGiven() {
        evaluationSheetLinkService.save(command(null, mapping()));

        assertEquals("시트1", saved().getTabName());
    }

    @Test
    @DisplayName("시트에 없는 탭을 지정하면 거절한다")
    void saveRejectsUnknownTab() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> evaluationSheetLinkService.save(
                        command("없는탭", mapping()))
        );

        assertEquals(
                ErrorCode.EVALUATION_SHEET_TAB_NOT_FOUND,
                exception.getErrorCode()
        );
        verify(evaluationSheetLinkRepository, never()).save(any());
    }

    @Test
    @DisplayName("시트에 없는 컬럼을 짝지으면 거절하고 사용 가능한 컬럼을 알려준다")
    void saveRejectsUnknownColumn() {
        EvaluationColumnMapping wrong = new EvaluationColumnMapping(
                "이메일", "평가유형", "평가 항목", "점수", null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> evaluationSheetLinkService.save(command(null, wrong))
        );

        /*
         * 저장 시점에 막아야 한다. 통과시키면 나중에 동기화가 실패하는데
         * 그때는 무엇이 잘못됐는지 사용자가 알 수 없다.
         */
        assertEquals(
                ErrorCode.EVALUATION_SHEET_COLUMN_NOT_FOUND,
                exception.getErrorCode()
        );
        assertTrue(exception.getMessage().contains("평가항목"));
        verify(evaluationSheetLinkRepository, never()).save(any());
    }

    @Test
    @DisplayName("시트를 열지 못하면 저장하지 않는다")
    void saveRejectsWhenSheetUnreadable() {
        when(validateExternalSheetUseCase.validate(any()))
                .thenThrow(new BusinessException(
                        ErrorCode.GOOGLE_SHEET_ACCESS_DENIED));

        assertThrows(
                BusinessException.class,
                () -> evaluationSheetLinkService.save(command(null, mapping()))
        );

        /*
         * 읽지도 못하는 주소를 저장해 두면 화면에는 연동된 것으로 보이지만
         * 동기화는 계속 실패한다.
         */
        verify(evaluationSheetLinkRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 연동돼 있으면 그 줄을 덮어쓴다")
    void saveOverwritesExistingLink() {
        Instant syncedAt = Instant.parse("2026-08-09T05:00:00Z");

        when(evaluationSheetLinkRepository.find())
                .thenReturn(Optional.of(EvaluationSheetLink.restore(
                        7L, "https://old", "옛탭", mapping(), syncedAt)));

        evaluationSheetLinkService.save(command(null, mapping()));

        EvaluationSheetLink result = saved();

        /*
         * domain 컬럼에 유니크 제약이 있어 새로 만들면 제약에 걸린다.
         * 마지막 동기화 시각은 설정을 고쳤다고 달라지지 않는다.
         */
        assertEquals(7L, result.getId());
        assertEquals(SHEET_URL, result.getSheetUrl());
        assertEquals(syncedAt, result.getLastSyncedAt());
    }

    @Test
    @DisplayName("의견 컬럼은 없어도 저장된다")
    void saveAllowsMissingCommentColumn() {
        EvaluationColumnMapping noComment = new EvaluationColumnMapping(
                "이메일", "평가유형", "평가항목", "점수", null);

        evaluationSheetLinkService.save(command(null, noComment));

        assertEquals(
                null,
                saved().getColumnMapping().comment()
        );
    }

    private EvaluationSheetLink saved() {
        ArgumentCaptor<EvaluationSheetLink> captor =
                ArgumentCaptor.forClass(EvaluationSheetLink.class);
        verify(evaluationSheetLinkRepository).save(captor.capture());

        return captor.getValue();
    }

    private SaveEvaluationSheetLinkCommand command(
            String tabName,
            EvaluationColumnMapping mapping
    ) {
        return new SaveEvaluationSheetLinkCommand(SHEET_URL, tabName, mapping);
    }

    private EvaluationColumnMapping mapping() {
        return new EvaluationColumnMapping(
                "이메일", "평가유형", "평가항목", "점수", "의견");
    }
}

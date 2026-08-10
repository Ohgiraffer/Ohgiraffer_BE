package com.ohgiraffer.evaluation.application.service;

import com.ohgiraffer.evaluation.application.port.EvaluationSheetReaderPort;
import com.ohgiraffer.evaluation.application.port.TraineeLookupPort;
import com.ohgiraffer.evaluation.application.query.EvaluationSyncResult;
import com.ohgiraffer.evaluation.domain.model.EvaluationColumnMapping;
import com.ohgiraffer.evaluation.domain.model.EvaluationRecord;
import com.ohgiraffer.evaluation.domain.model.EvaluationSheetLink;
import com.ohgiraffer.evaluation.domain.repository.EvaluationRecordRepository;
import com.ohgiraffer.evaluation.domain.repository.EvaluationSheetLinkRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 추가와 수정을 가려내는지, 값이 같으면 건드리지 않는지, 잘못된 행만 건너뛰는지 확인한다.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EvaluationSyncServiceTest {

    private static final Long SHEET_LINK_ID = 1L;
    private static final Long TRAINEE_ID = 101L;
    private static final String EMAIL = "student101@campflow.test";

    private static final List<String> HEADER =
            List.of("이메일", "이름", "평가유형", "평가항목", "점수", "의견");

    @Mock
    private EvaluationSheetLinkRepository evaluationSheetLinkRepository;

    @Mock
    private EvaluationRecordRepository evaluationRecordRepository;

    @Mock
    private EvaluationSheetReaderPort evaluationSheetReaderPort;

    @Mock
    private TraineeLookupPort traineeLookupPort;

    private EvaluationSyncService evaluationSyncService;

    @BeforeEach
    void setUp() {
        evaluationSyncService = new EvaluationSyncService(
                evaluationSheetLinkRepository,
                evaluationRecordRepository,
                evaluationSheetReaderPort,
                traineeLookupPort
        );

        when(evaluationSheetLinkRepository.find())
                .thenReturn(Optional.of(sheetLink()));
        when(traineeLookupPort.findTraineeIdsByEmails(any()))
                .thenReturn(Map.of(EMAIL, TRAINEE_ID));
        when(evaluationRecordRepository.findAllBySheetLinkId(SHEET_LINK_ID))
                .thenReturn(List.of());
        when(evaluationRecordRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("처음 보는 평가는 새로 저장한다")
    void syncAddsNewRecords() {
        givenSheet(
                row(EMAIL, "김철수", "중간평가", "코드 품질", "85", "잘함"),
                row(EMAIL, "김철수", "중간평가", "협업", "90", "")
        );

        EvaluationSyncResult result = evaluationSyncService.sync();

        assertEquals(2, result.addedCount());
        assertEquals(0, result.updatedCount());
        assertEquals(2, saved().size());
    }

    @Test
    @DisplayName("점수가 바뀌면 기존 평가를 갱신한다")
    void syncUpdatesChangedScore() {
        givenStored(stored("코드 품질", new BigDecimal("70"), "리팩터링 필요"));
        givenSheet(row(EMAIL, "김철수", "중간평가", "코드 품질", "88", "리팩터링 필요"));

        EvaluationSyncResult result = evaluationSyncService.sync();

        assertEquals(0, result.addedCount());
        assertEquals(1, result.updatedCount());
        assertEquals(new BigDecimal("88"), saved().get(0).getScore());
    }

    @Test
    @DisplayName("값이 그대로면 저장하지 않는다")
    void syncSkipsUnchangedRecords() {
        givenStored(stored("코드 품질", new BigDecimal("85.00"), "잘함"));
        givenSheet(row(EMAIL, "김철수", "중간평가", "코드 품질", "85", "잘함"));

        EvaluationSyncResult result = evaluationSyncService.sync();

        /*
         * BigDecimal 은 equals 가 소수 자릿수까지 따져 85 와 85.00 을 다르게 본다.
         * compareTo 로 비교하지 않으면 시트를 안 고쳐도 매번 수정으로 기록된다.
         */
        assertEquals(0, result.changedCount());
        assertTrue(saved().isEmpty());
    }

    @Test
    @DisplayName("훈련생을 못 찾으면 그 행만 건너뛴다")
    void syncSkipsUnknownTrainee() {
        givenSheet(
                row(EMAIL, "김철수", "중간평가", "코드 품질", "85", ""),
                row("없는사람@campflow.test", "???", "중간평가", "협업", "90", "")
        );

        EvaluationSyncResult result = evaluationSyncService.sync();

        /*
         * 시트가 100행인데 오타 하나로 전부 막히면 쓰기 어렵다.
         * 나머지는 반영하고 건너뛴 행만 알린다.
         */
        assertEquals(1, result.addedCount());
        assertEquals(1, result.skipped().size());
        assertEquals(3, result.skipped().get(0).rowNumber());
        assertTrue(result.skipped().get(0).reason().contains("없는사람"));
    }

    @Test
    @DisplayName("점수가 숫자가 아니면 그 행만 건너뛴다")
    void syncSkipsNonNumericScore() {
        givenSheet(row(EMAIL, "김철수", "중간평가", "코드 품질", "미제출", ""));

        EvaluationSyncResult result = evaluationSyncService.sync();

        assertEquals(0, result.addedCount());
        assertEquals(1, result.skipped().size());
        assertTrue(result.skipped().get(0).reason().contains("숫자"));
    }

    @Test
    @DisplayName("같은 평가가 시트에 두 번 있으면 뒤엣것을 버린다")
    void syncSkipsDuplicateRowKey() {
        givenSheet(
                row(EMAIL, "김철수", "중간평가", "코드 품질", "85", ""),
                row(EMAIL, "김철수", "중간평가", "코드 품질", "90", "")
        );

        EvaluationSyncResult result = evaluationSyncService.sync();

        /*
         * 그대로 두면 한 트랜잭션에서 같은 행을 두 번 저장하게 된다.
         */
        assertEquals(1, result.addedCount());
        assertEquals(1, result.skipped().size());
        assertTrue(result.skipped().get(0).reason().contains("같은 평가"));
    }

    @Test
    @DisplayName("빈 행은 조용히 넘어간다")
    void syncIgnoresBlankRows() {
        givenSheet(
                row(EMAIL, "김철수", "중간평가", "코드 품질", "85", ""),
                List.of("", "", "", "", "", "")
        );

        EvaluationSyncResult result = evaluationSyncService.sync();

        assertEquals(1, result.addedCount());
        assertTrue(result.skipped().isEmpty());
    }

    @Test
    @DisplayName("연동 설정이 없으면 동기화할 수 없다")
    void syncRequiresSheetLink() {
        when(evaluationSheetLinkRepository.find()).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> evaluationSyncService.sync()
        );

        assertEquals(
                ErrorCode.EVALUATION_SHEET_LINK_NOT_FOUND,
                exception.getErrorCode()
        );
        verify(evaluationSheetReaderPort, never()).readRows(any(), any());
    }

    @Test
    @DisplayName("시트에서 컬럼 이름이 바뀌면 동기화를 멈춘다")
    void syncStopsWhenMappedColumnMissing() {
        when(evaluationSheetReaderPort.readRows(any(), any()))
                .thenReturn(List.of(
                        List.of("이메일", "이름", "평가구분", "평가항목", "점수", "의견")));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> evaluationSyncService.sync()
        );

        /*
         * 저장할 때 확인했더라도 그 뒤에 시트를 고쳤을 수 있다.
         * 이 경우는 행 하나가 아니라 설정 자체가 어긋난 것이라 전체를 멈춘다.
         */
        assertEquals(
                ErrorCode.EVALUATION_SHEET_COLUMN_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    private void givenSheet(List<String>... dataRows) {
        List<List<String>> rows = new java.util.ArrayList<>();
        rows.add(HEADER);
        rows.addAll(List.of(dataRows));

        when(evaluationSheetReaderPort.readRows(any(), any())).thenReturn(rows);
    }

    private void givenStored(EvaluationRecord record) {
        when(evaluationRecordRepository.findAllBySheetLinkId(SHEET_LINK_ID))
                .thenReturn(List.of(record));
    }

    private List<EvaluationRecord> saved() {
        ArgumentCaptor<List<EvaluationRecord>> captor =
                ArgumentCaptor.forClass(List.class);
        verify(evaluationRecordRepository).saveAll(captor.capture());

        return captor.getValue();
    }

    private List<String> row(String... values) {
        return List.of(values);
    }

    private EvaluationRecord stored(
            String item,
            BigDecimal score,
            String comment
    ) {
        return EvaluationRecord.restore(
                50L,
                TRAINEE_ID,
                SHEET_LINK_ID,
                "중간평가",
                item,
                score,
                comment,
                EvaluationRecord.sheetRowKey(EMAIL, "중간평가", item),
                Instant.parse("2026-08-09T05:00:00Z")
        );
    }

    private EvaluationSheetLink sheetLink() {
        return EvaluationSheetLink.restore(
                SHEET_LINK_ID,
                "https://docs.google.com/spreadsheets/d/1AbC/edit",
                "시트1",
                new EvaluationColumnMapping(
                        "이메일", "평가유형", "평가항목", "점수", "의견"),
                null
        );
    }
}

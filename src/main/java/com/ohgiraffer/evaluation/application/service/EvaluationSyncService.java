package com.ohgiraffer.evaluation.application.service;

import com.ohgiraffer.evaluation.application.port.EvaluationSheetReaderPort;
import com.ohgiraffer.evaluation.application.port.EvaluationSummaryPort;
import com.ohgiraffer.evaluation.application.port.TraineeLookupPort;
import com.ohgiraffer.evaluation.application.query.EvaluationSyncResult;
import com.ohgiraffer.evaluation.application.usecase.EvaluationSyncUseCase;
import com.ohgiraffer.evaluation.domain.model.EvaluationChange;
import com.ohgiraffer.evaluation.domain.model.EvaluationColumnMapping;
import com.ohgiraffer.evaluation.domain.model.EvaluationDiffSummaryWriter;
import com.ohgiraffer.evaluation.domain.model.EvaluationRecord;
import com.ohgiraffer.evaluation.domain.model.EvaluationSheetLink;
import com.ohgiraffer.evaluation.domain.model.SheetSyncLog;
import com.ohgiraffer.evaluation.domain.model.TraineeChangeSummary;
import com.ohgiraffer.evaluation.domain.repository.EvaluationRecordRepository;
import com.ohgiraffer.evaluation.domain.repository.EvaluationSheetLinkRepository;
import com.ohgiraffer.evaluation.domain.repository.SheetSyncLogRepository;
import com.ohgiraffer.global.aop.ratelimit.RateLimited;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 시트의 평가 데이터를 내부 데이터로 옮기고, 무엇이 바뀌었는지 기록한다.
 *
 * <p>구글 시트는 한 번만 읽는다. 이전 상태는 저장된 평가에서 가져와 비교한다.
 * 호출 한도가 서비스 계정 전체로 분당 60회라 한 번의 동기화가 시트를 여러 번 부르면 안 된다.
 *
 * <p>한 행이 잘못돼도 전체를 멈추지 않는다. 시트가 100행인데 이메일 오타 하나로 아무것도
 * 반영되지 않으면 쓰기 어렵다. 그 행만 건너뛰고 이유를 결과에 담는다.
 */
@Service
public class EvaluationSyncService implements EvaluationSyncUseCase {

    /** 헤더가 1행이므로 데이터는 2행부터다. 화면에 보이는 줄 번호와 맞춘다. */
    private static final int FIRST_DATA_ROW_NUMBER = 2;

    private static final Logger log =
            LoggerFactory.getLogger(EvaluationSyncService.class);

    private final EvaluationSheetLinkRepository evaluationSheetLinkRepository;
    private final EvaluationRecordRepository evaluationRecordRepository;
    private final SheetSyncLogRepository sheetSyncLogRepository;
    private final EvaluationSheetReaderPort evaluationSheetReaderPort;
    private final TraineeLookupPort traineeLookupPort;
    private final EvaluationSummaryPort evaluationSummaryPort;

    public EvaluationSyncService(
            EvaluationSheetLinkRepository evaluationSheetLinkRepository,
            EvaluationRecordRepository evaluationRecordRepository,
            SheetSyncLogRepository sheetSyncLogRepository,
            EvaluationSheetReaderPort evaluationSheetReaderPort,
            TraineeLookupPort traineeLookupPort,
            EvaluationSummaryPort evaluationSummaryPort
    ) {
        this.evaluationSheetLinkRepository = evaluationSheetLinkRepository;
        this.evaluationRecordRepository = evaluationRecordRepository;
        this.sheetSyncLogRepository = sheetSyncLogRepository;
        this.evaluationSheetReaderPort = evaluationSheetReaderPort;
        this.traineeLookupPort = traineeLookupPort;
        this.evaluationSummaryPort = evaluationSummaryPort;
    }

    @Override
    @Transactional
    @RateLimited(key = "google_sheets_sync", limit = 10, windowSeconds = 60)
    public EvaluationSyncResult sync(Long executedBy) {
        EvaluationSheetLink sheetLink = evaluationSheetLinkRepository.find()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.EVALUATION_SHEET_LINK_NOT_FOUND));

        List<List<String>> rows = evaluationSheetReaderPort.readRows(
                sheetLink.getSheetUrl(),
                sheetLink.getTabName()
        );

        if (rows.isEmpty()) {
            return finish(sheetLink, executedBy, List.of(), List.of(), List.of());
        }

        /*
         * 컬럼 위치는 헤더에서 찾는다. 저장할 때 확인했더라도 그 뒤에 시트에서 컬럼 이름을
         * 바꿨을 수 있어 여기서 다시 본다.
         */
        Map<String, Integer> columnIndex = toColumnIndex(rows.get(0));
        EvaluationColumnMapping mapping = sheetLink.getColumnMapping();

        mapping.validateAgainst(new ArrayList<>(columnIndex.keySet()));

        List<List<String>> dataRows = rows.subList(1, rows.size());

        /*
         * 훈련생은 한 번에 찾는다. 시트에는 같은 사람이 항목 수만큼 반복되므로
         * 행마다 물으면 질의가 행 수만큼 늘어난다.
         */
        Map<String, TraineeLookupPort.Trainee> trainees =
                traineeLookupPort.findTraineesByEmails(
                        collectEmails(dataRows, columnIndex, mapping));

        Map<String, EvaluationRecord> existing = evaluationRecordRepository
                .findAllBySheetLinkId(sheetLink.getId())
                .stream()
                .collect(Collectors.toMap(
                        EvaluationRecord::getSheetRowKey,
                        Function.identity(),
                        (first, second) -> first
                ));

        List<EvaluationRecord> toSave = new ArrayList<>();
        List<EvaluationChange> changes = new ArrayList<>();
        List<EvaluationSyncResult.SkippedRow> skipped = new ArrayList<>();
        Set<String> seenKeys = new LinkedHashSet<>();

        for (int index = 0; index < dataRows.size(); index++) {
            int rowNumber = FIRST_DATA_ROW_NUMBER + index;
            List<String> row = dataRows.get(index);

            if (isBlankRow(row)) {
                continue;
            }

            ParsedRow parsed;

            try {
                parsed = toRecord(row, columnIndex, mapping, sheetLink, trainees);
            } catch (BusinessException exception) {
                skipped.add(new EvaluationSyncResult.SkippedRow(
                        rowNumber, exception.getMessage()));
                continue;
            }

            /*
             * 같은 식별값이 시트에 두 번 나오면 뒤엣것을 버린다. 그대로 두면 한 트랜잭션에서
             * 같은 행을 두 번 저장하게 된다.
             */
            if (!seenKeys.add(parsed.record().getSheetRowKey())) {
                skipped.add(new EvaluationSyncResult.SkippedRow(
                        rowNumber,
                        "앞선 행과 같은 평가입니다: " + parsed.record().getSheetRowKey()));
                continue;
            }

            EvaluationRecord stored = existing.get(parsed.record().getSheetRowKey());

            if (stored == null) {
                toSave.add(parsed.record());
                changes.add(EvaluationChange.added(
                        parsed.traineeName(), parsed.record()));
            } else if (stored.differsFrom(parsed.record())) {
                EvaluationRecord updated = stored.updateFrom(parsed.record());

                toSave.add(updated);
                changes.add(EvaluationChange.updated(
                        parsed.traineeName(), stored, updated));
            }
        }

        evaluationRecordRepository.saveAll(toSave);

        return finish(sheetLink, executedBy, toSave, changes, skipped);
    }

    /**
     * 마지막 동기화 시각을 갱신하고, 바뀐 것이 있으면 이력을 남긴다.
     *
     * <p>변경이 없으면 이력을 남기지 않는다. 화면의 이력 목록은 "언제 무엇이 몇 건 바뀌었나"
     * 를 보는 곳이라, 눌렀지만 바뀐 것이 없는 실행까지 쌓이면 정작 볼 것이 묻힌다.
     *
     * <p>마지막 시각은 변경이 없어도 갱신한다. "언제 확인했는가" 는 별개의 정보다.
     */
    private EvaluationSyncResult finish(
            EvaluationSheetLink sheetLink,
            Long executedBy,
            List<EvaluationRecord> saved,
            List<EvaluationChange> changes,
            List<EvaluationSyncResult.SkippedRow> skipped
    ) {
        evaluationSheetLinkRepository.save(sheetLink.markSynced(Instant.now()));

        int added = (int) changes.stream()
                .filter(change -> change.type() == EvaluationChange.Type.ADDED)
                .count();

        List<TraineeChangeSummary> summaries = summarize(changes);

        Long syncLogId = null;

        if (!changes.isEmpty()) {
            syncLogId = sheetSyncLogRepository.save(SheetSyncLog.create(
                    sheetLink.getId(),
                    executedBy,
                    changes.size(),
                    summaries
            )).getId();
        }

        return new EvaluationSyncResult(
                sheetLink.getId(),
                syncLogId,
                added,
                changes.size() - added,
                summaries,
                skipped
        );
    }

    /**
     * 훈련생별 변경 카드를 만든다.
     *
     * <p>무엇이 어떻게 바뀌었는지는 우리가 이미 값으로 들고 있어 직접 적는다. AI 에게는
     * 확인이 필요한 대목만 묻고, 그 답을 카드에 한 줄씩 끼워 넣는다.
     *
     * <p>AI 가 실패해도 카드는 그대로 나간다. 확인 필요 줄만 비게 된다. 확인 필요는 거들어
     * 주는 값이지 평가 데이터가 아니라서, 외부 호출 때문에 이미 반영된 평가까지 되돌릴 이유가 없다.
     */
    private List<TraineeChangeSummary> summarize(List<EvaluationChange> changes) {
        List<TraineeChangeSummary> summaries =
                EvaluationDiffSummaryWriter.write(changes);

        if (summaries.isEmpty()) {
            return summaries;
        }

        Map<String, String> pointsToCheck;

        try {
            pointsToCheck = evaluationSummaryPort.findPointsToCheck(changes);
        } catch (RuntimeException exception) {
            log.warn(
                    "확인이 필요한 항목을 찾지 못해 그 줄을 비웁니다. 변경 {}건",
                    changes.size(),
                    exception
            );

            return summaries;
        }

        return summaries.stream()
                .map(summary -> summary.withNeedsCheck(
                        pointsToCheck.get(summary.traineeName())))
                .toList();
    }

    private ParsedRow toRecord(
            List<String> row,
            Map<String, Integer> columnIndex,
            EvaluationColumnMapping mapping,
            EvaluationSheetLink sheetLink,
            Map<String, TraineeLookupPort.Trainee> trainees
    ) {
        String email = cell(row, columnIndex, mapping.traineeIdentifier());

        if (email.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE, "훈련생 식별자가 비어 있습니다.");
        }

        TraineeLookupPort.Trainee trainee =
                trainees.get(email.toLowerCase(Locale.ROOT));

        if (trainee == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "훈련생을 찾을 수 없습니다: " + email);
        }

        EvaluationRecord record = EvaluationRecord.create(
                trainee.id(),
                sheetLink.getId(),
                email,
                cell(row, columnIndex, mapping.evaluationType()),
                cell(row, columnIndex, mapping.item()),
                toScore(cell(row, columnIndex, mapping.score())),
                mapping.comment() == null
                        ? null
                        : cell(row, columnIndex, mapping.comment())
        );

        return new ParsedRow(record, trainee.name());
    }

    /**
     * 점수를 숫자로 바꾼다.
     *
     * <p>비어 있으면 null 로 둔다. 아직 채점하지 않은 항목일 수 있고, 그것 때문에
     * 행 전체를 버릴 이유는 없다. 숫자가 아닌 값이 들어 있으면 그때는 건너뛴다.
     */
    private BigDecimal toScore(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "점수를 숫자로 읽을 수 없습니다: " + value);
        }
    }

    private Set<String> collectEmails(
            List<List<String>> dataRows,
            Map<String, Integer> columnIndex,
            EvaluationColumnMapping mapping
    ) {
        Set<String> emails = new LinkedHashSet<>();

        for (List<String> row : dataRows) {
            String email = cell(row, columnIndex, mapping.traineeIdentifier());

            if (!email.isBlank()) {
                emails.add(email);
            }
        }

        return emails;
    }

    /**
     * 헤더 이름으로 컬럼 위치를 찾을 수 있게 만든다.
     *
     * <p>같은 이름이 두 번 나오면 앞엣것을 쓴다. 뒤엣것으로 덮으면 사용자가 화면에서 고른
     * 컬럼과 다른 자리를 읽게 된다.
     */
    private Map<String, Integer> toColumnIndex(List<String> header) {
        Map<String, Integer> index = new HashMap<>();

        for (int position = 0; position < header.size(); position++) {
            String name = header.get(position);

            if (!name.isBlank()) {
                index.putIfAbsent(name, position);
            }
        }

        return index;
    }

    /**
     * 줄 끝의 빈 칸은 응답에 담기지 않아 행마다 길이가 다르다. 없는 자리는 빈 값으로 본다.
     */
    private String cell(
            List<String> row,
            Map<String, Integer> columnIndex,
            String columnName
    ) {
        Integer position = columnIndex.get(columnName);

        if (position == null || position >= row.size()) {
            return "";
        }

        return row.get(position);
    }

    private boolean isBlankRow(List<String> row) {
        return row.stream().allMatch(String::isBlank);
    }

    /**
     * 시트 한 줄을 읽은 결과. 이름은 변경 요약에만 쓰여 평가 자체에는 담기지 않는다.
     */
    private record ParsedRow(EvaluationRecord record, String traineeName) {
    }
}
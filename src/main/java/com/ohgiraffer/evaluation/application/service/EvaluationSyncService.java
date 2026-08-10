package com.ohgiraffer.evaluation.application.service;

import com.ohgiraffer.evaluation.application.port.EvaluationSheetReaderPort;
import com.ohgiraffer.evaluation.application.port.TraineeLookupPort;
import com.ohgiraffer.evaluation.application.query.EvaluationSyncResult;
import com.ohgiraffer.evaluation.application.usecase.EvaluationSyncUseCase;
import com.ohgiraffer.evaluation.domain.model.EvaluationColumnMapping;
import com.ohgiraffer.evaluation.domain.model.EvaluationRecord;
import com.ohgiraffer.evaluation.domain.model.EvaluationSheetLink;
import com.ohgiraffer.evaluation.domain.repository.EvaluationRecordRepository;
import com.ohgiraffer.evaluation.domain.repository.EvaluationSheetLinkRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
 * 시트의 평가 데이터를 내부 데이터로 옮긴다.
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

    private final EvaluationSheetLinkRepository evaluationSheetLinkRepository;
    private final EvaluationRecordRepository evaluationRecordRepository;
    private final EvaluationSheetReaderPort evaluationSheetReaderPort;
    private final TraineeLookupPort traineeLookupPort;

    public EvaluationSyncService(
            EvaluationSheetLinkRepository evaluationSheetLinkRepository,
            EvaluationRecordRepository evaluationRecordRepository,
            EvaluationSheetReaderPort evaluationSheetReaderPort,
            TraineeLookupPort traineeLookupPort
    ) {
        this.evaluationSheetLinkRepository = evaluationSheetLinkRepository;
        this.evaluationRecordRepository = evaluationRecordRepository;
        this.evaluationSheetReaderPort = evaluationSheetReaderPort;
        this.traineeLookupPort = traineeLookupPort;
    }

    @Override
    @Transactional
    public EvaluationSyncResult sync() {
        EvaluationSheetLink sheetLink = evaluationSheetLinkRepository.find()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.EVALUATION_SHEET_LINK_NOT_FOUND));

        List<List<String>> rows = evaluationSheetReaderPort.readRows(
                sheetLink.getSheetUrl(),
                sheetLink.getTabName()
        );

        if (rows.isEmpty()) {
            return new EvaluationSyncResult(
                    sheetLink.getId(), 0, 0, List.of());
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
        Map<String, Long> traineeIds =
                traineeLookupPort.findTraineeIdsByEmails(
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
        List<EvaluationSyncResult.SkippedRow> skipped = new ArrayList<>();
        Set<String> seenKeys = new LinkedHashSet<>();
        int added = 0;
        int updated = 0;

        for (int index = 0; index < dataRows.size(); index++) {
            int rowNumber = FIRST_DATA_ROW_NUMBER + index;
            List<String> row = dataRows.get(index);

            if (isBlankRow(row)) {
                continue;
            }

            EvaluationRecord parsed;

            try {
                parsed = toRecord(row, columnIndex, mapping, sheetLink, traineeIds);
            } catch (BusinessException exception) {
                skipped.add(new EvaluationSyncResult.SkippedRow(
                        rowNumber, exception.getMessage()));
                continue;
            }

            /*
             * 같은 식별값이 시트에 두 번 나오면 뒤엣것을 버린다. 그대로 두면 한 트랜잭션에서
             * 같은 행을 두 번 저장하게 된다.
             */
            if (!seenKeys.add(parsed.getSheetRowKey())) {
                skipped.add(new EvaluationSyncResult.SkippedRow(
                        rowNumber, "앞선 행과 같은 평가입니다: " + parsed.getSheetRowKey()));
                continue;
            }

            EvaluationRecord stored = existing.get(parsed.getSheetRowKey());

            if (stored == null) {
                toSave.add(parsed);
                added++;
            } else if (stored.differsFrom(parsed)) {
                toSave.add(stored.updateFrom(parsed));
                updated++;
            }
        }

        evaluationRecordRepository.saveAll(toSave);

        return new EvaluationSyncResult(
                sheetLink.getId(), added, updated, skipped);
    }

    private EvaluationRecord toRecord(
            List<String> row,
            Map<String, Integer> columnIndex,
            EvaluationColumnMapping mapping,
            EvaluationSheetLink sheetLink,
            Map<String, Long> traineeIds
    ) {
        String email = cell(row, columnIndex, mapping.traineeIdentifier());

        if (email.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE, "훈련생 식별자가 비어 있습니다.");
        }

        Long traineeId = traineeIds.get(email.toLowerCase(Locale.ROOT));

        if (traineeId == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "훈련생을 찾을 수 없습니다: " + email);
        }

        return EvaluationRecord.create(
                traineeId,
                sheetLink.getId(),
                email,
                cell(row, columnIndex, mapping.evaluationType()),
                cell(row, columnIndex, mapping.item()),
                toScore(cell(row, columnIndex, mapping.score())),
                mapping.comment() == null
                        ? null
                        : cell(row, columnIndex, mapping.comment())
        );
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
}

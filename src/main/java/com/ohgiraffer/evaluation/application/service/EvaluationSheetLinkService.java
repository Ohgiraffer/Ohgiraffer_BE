package com.ohgiraffer.evaluation.application.service;

import com.ohgiraffer.evaluation.application.command.SaveEvaluationSheetLinkCommand;
import com.ohgiraffer.evaluation.application.usecase.EvaluationSheetLinkUseCase;
import com.ohgiraffer.evaluation.domain.model.EvaluationSheetLink;
import com.ohgiraffer.evaluation.domain.repository.EvaluationSheetLinkRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.google.sheets.ExternalSheetValidationResult;
import com.ohgiraffer.global.google.sheets.SheetColumn;
import com.ohgiraffer.global.google.sheets.ValidateExternalSheetUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 평가 시트 연동 설정.
 *
 * <p>시트를 여는 일은 공용 코드({@link ValidateExternalSheetUseCase})가 맡는다. 그쪽은
 * 도메인별 필수 컬럼을 모르므로 컬럼 후보만 돌려준다. 무엇이 필수인지 판단하는 것은
 * 이 도메인의 몫이다.
 */
@Service
public class EvaluationSheetLinkService implements EvaluationSheetLinkUseCase {

    private final EvaluationSheetLinkRepository evaluationSheetLinkRepository;
    private final ValidateExternalSheetUseCase validateExternalSheetUseCase;

    public EvaluationSheetLinkService(
            EvaluationSheetLinkRepository evaluationSheetLinkRepository,
            ValidateExternalSheetUseCase validateExternalSheetUseCase
    ) {
        this.evaluationSheetLinkRepository = evaluationSheetLinkRepository;
        this.validateExternalSheetUseCase = validateExternalSheetUseCase;
    }

    @Override
    @Transactional
    public EvaluationSheetLink save(SaveEvaluationSheetLinkCommand command) {
        /*
         * 시트를 여는 것이 먼저다. 읽지도 못하는 주소를 저장해 두면 화면에는 연동된 것으로
         * 보이지만 동기화는 계속 실패한다.
         */
        ExternalSheetValidationResult validation =
                validateExternalSheetUseCase.validate(command.spreadsheetUrl());

        SheetColumn targetSheet = resolveSheet(validation, command.tabName());

        command.columnMapping().validateAgainst(targetSheet.columns());

        /*
         * domain 컬럼에 유니크 제약이 있어 평가 연동은 한 줄뿐이다. 이미 있으면 그 줄을
         * 고쳐 저장해야 하고, 새로 만들면 제약에 걸린다.
         */
        EvaluationSheetLink sheetLink = evaluationSheetLinkRepository.find()
                .map(existing -> existing.update(
                        command.spreadsheetUrl(),
                        targetSheet.sheetName(),
                        command.columnMapping()
                ))
                .orElseGet(() -> EvaluationSheetLink.create(
                        command.spreadsheetUrl(),
                        targetSheet.sheetName(),
                        command.columnMapping()
                ));

        return evaluationSheetLinkRepository.save(sheetLink);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EvaluationSheetLink> find() {
        return evaluationSheetLinkRepository.find();
    }

    /**
     * 읽어올 탭을 고른다.
     *
     * <p>화면에 탭 선택이 없어 보통은 이름이 오지 않는다. 그때는 첫 번째 탭을 쓴다.
     * 스프레드시트에 탭이 여럿일 수 있어 목록 자체는 배열로 받아 둔다.
     */
    private SheetColumn resolveSheet(
            ExternalSheetValidationResult validation,
            String tabName
    ) {
        if (tabName == null || tabName.isBlank()) {
            return validation.sheets().get(0);
        }

        return validation.sheets().stream()
                .filter(sheet -> sheet.sheetName().equals(tabName.trim()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.EVALUATION_SHEET_TAB_NOT_FOUND,
                        "시트에 '" + tabName + "' 탭이 없습니다."
                                + " 사용할 수 있는 탭: "
                                + validation.sheets().stream()
                                        .map(SheetColumn::sheetName)
                                        .reduce((a, b) -> a + ", " + b)
                                        .orElse("")
                ));
    }
}

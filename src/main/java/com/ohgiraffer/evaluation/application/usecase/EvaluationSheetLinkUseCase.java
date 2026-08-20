package com.ohgiraffer.evaluation.application.usecase;

import com.ohgiraffer.evaluation.application.command.SaveEvaluationSheetLinkCommand;
import com.ohgiraffer.evaluation.domain.model.EvaluationSheetLink;

import java.util.Optional;

public interface EvaluationSheetLinkUseCase {

    /**
     * 연동 설정을 저장한다. 이미 있으면 덮어쓴다.
     *
     * <p>저장 전에 시트를 실제로 열어 보고, 지정한 탭과 컬럼이 그 안에 있는지 확인한다.
     * 통과시켜 두면 나중에 동기화가 실패하는데 그때는 원인을 짚기 어렵다.
     */
    EvaluationSheetLink save(SaveEvaluationSheetLinkCommand command);

    /**
     * 저장된 연동 설정. 아직 연동한 적이 없으면 비어 있다.
     */
    Optional<EvaluationSheetLink> find();
}

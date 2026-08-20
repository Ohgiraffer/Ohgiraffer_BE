package com.ohgiraffer.evaluation.application.command;

import com.ohgiraffer.evaluation.domain.model.EvaluationColumnMapping;

/**
 * 평가 시트 연동 저장 요청.
 *
 * <p>{@code tabName} 은 생략할 수 있다. 화면에 탭 선택이 없어서 보내지 않으며,
 * 그때는 스프레드시트의 첫 번째 탭을 쓴다.
 */
public record SaveEvaluationSheetLinkCommand(
        String spreadsheetUrl,
        String tabName,
        EvaluationColumnMapping columnMapping
) {
}

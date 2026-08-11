package com.ohgiraffer.evaluation.domain.repository;

import com.ohgiraffer.evaluation.domain.model.EvaluationSheetLink;

import java.util.Optional;

/**
 * 평가 시트 연동 정보 영속성 포트. 구현은 infrastructure 계층의 어댑터가 담당한다.
 *
 * <p>식별자로 찾는 메서드가 없다. {@code external_sheet_link.domain} 에 유니크 제약이 있어
 * 평가 연동은 언제나 한 줄뿐이고, 화면도 그 한 줄만 다루기 때문이다.
 */
public interface EvaluationSheetLinkRepository {

    EvaluationSheetLink save(EvaluationSheetLink sheetLink);

    Optional<EvaluationSheetLink> find();
}

package com.ohgiraffer.evaluation.domain.repository;

import com.ohgiraffer.evaluation.domain.model.EvaluationRecord;

import java.util.List;

/**
 * 평가 기록 영속성 포트. 구현은 infrastructure 계층의 어댑터가 담당한다.
 */
public interface EvaluationRecordRepository {

    /**
     * 해당 연동으로 지금까지 저장된 평가 전부.
     *
     * <p>동기화할 때 시트와 대조하려면 이전 상태가 필요하다. 시트를 두 번 읽는 대신
     * 여기서 읽어 비교한다. 구글 시트 호출 한도가 서비스 계정 전체로 분당 60회라
     * 한 번의 동기화가 시트를 여러 번 부르지 않게 한다.
     */
    List<EvaluationRecord> findAllBySheetLinkId(Long sheetLinkId);

    List<EvaluationRecord> saveAll(List<EvaluationRecord> records);
}

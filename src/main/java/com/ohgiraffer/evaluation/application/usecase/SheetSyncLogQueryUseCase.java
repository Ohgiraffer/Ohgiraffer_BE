package com.ohgiraffer.evaluation.application.usecase;

import com.ohgiraffer.evaluation.application.query.SheetSyncLogView;

import java.util.List;

public interface SheetSyncLogQueryUseCase {

    /**
     * 이력 목록. 최신순이다. 연동한 적이 없으면 비어 있다.
     */
    List<SheetSyncLogView> findAll();

    SheetSyncLogView findDetail(Long syncLogId);
}

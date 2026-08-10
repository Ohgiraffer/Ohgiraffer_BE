package com.ohgiraffer.evaluation.domain.repository;

import com.ohgiraffer.evaluation.domain.model.SheetSyncLog;

import java.util.List;
import java.util.Optional;

public interface SheetSyncLogRepository {

    SheetSyncLog save(SheetSyncLog syncLog);

    /**
     * 해당 연동의 이력을 최신순으로 반환한다.
     *
     * <p>페이지네이션을 두지 않는다. 사람이 버튼을 눌러야만 쌓이고 변경이 있을 때만 남아,
     * 화면에서 잘라 쓰는 것으로 충분하다.
     */
    List<SheetSyncLog> findAllBySheetLinkId(Long sheetLinkId);

    Optional<SheetSyncLog> findById(Long syncLogId);
}

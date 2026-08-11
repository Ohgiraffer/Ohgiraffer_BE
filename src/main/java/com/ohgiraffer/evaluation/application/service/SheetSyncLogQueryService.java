package com.ohgiraffer.evaluation.application.service;

import com.ohgiraffer.evaluation.application.port.ExecutorNameQueryPort;
import com.ohgiraffer.evaluation.application.query.SheetSyncLogView;
import com.ohgiraffer.evaluation.application.usecase.SheetSyncLogQueryUseCase;
import com.ohgiraffer.evaluation.domain.model.SheetSyncLog;
import com.ohgiraffer.evaluation.domain.repository.EvaluationSheetLinkRepository;
import com.ohgiraffer.evaluation.domain.repository.SheetSyncLogRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class SheetSyncLogQueryService implements SheetSyncLogQueryUseCase {

    private final SheetSyncLogRepository sheetSyncLogRepository;
    private final EvaluationSheetLinkRepository evaluationSheetLinkRepository;
    private final ExecutorNameQueryPort executorNameQueryPort;

    public SheetSyncLogQueryService(
            SheetSyncLogRepository sheetSyncLogRepository,
            EvaluationSheetLinkRepository evaluationSheetLinkRepository,
            ExecutorNameQueryPort executorNameQueryPort
    ) {
        this.sheetSyncLogRepository = sheetSyncLogRepository;
        this.evaluationSheetLinkRepository = evaluationSheetLinkRepository;
        this.executorNameQueryPort = executorNameQueryPort;
    }

    @Override
    public List<SheetSyncLogView> findAll() {
        return evaluationSheetLinkRepository.find()
                .map(sheetLink -> toViews(
                        sheetSyncLogRepository.findAllBySheetLinkId(
                                sheetLink.getId())))
                .orElseGet(List::of);
    }

    @Override
    public SheetSyncLogView findDetail(Long syncLogId) {
        SheetSyncLog syncLog = sheetSyncLogRepository.findById(syncLogId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.EVALUATION_SYNC_LOG_NOT_FOUND));

        return toViews(List.of(syncLog)).get(0);
    }

    /**
     * 실행자 이름을 한 번에 가져와 채운다.
     *
     * <p>이력마다 조회하면 목록 길이만큼 질의가 늘어난다. 실행자는 몇 사람으로 겹치므로
     * 중복을 걷어내고 한 번에 묻는다.
     */
    private List<SheetSyncLogView> toViews(List<SheetSyncLog> syncLogs) {
        if (syncLogs.isEmpty()) {
            return List.of();
        }

        Map<Long, String> names = executorNameQueryPort.findNames(
                syncLogs.stream()
                        .map(SheetSyncLog::getExecutedBy)
                        .toList()
        );

        return syncLogs.stream()
                .map(syncLog -> SheetSyncLogView.of(
                        syncLog,
                        names.get(syncLog.getExecutedBy())
                ))
                .toList();
    }
}

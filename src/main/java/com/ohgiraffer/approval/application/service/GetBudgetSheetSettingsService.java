package com.ohgiraffer.approval.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffer.approval.application.command.BudgetColumnMapping;
import com.ohgiraffer.approval.application.query.BudgetSheetSettingsResult;
import com.ohgiraffer.approval.application.usecase.GetBudgetSheetSettingsUseCase;
import com.ohgiraffer.approval.domain.model.budget.ExternalSheetLink;
import com.ohgiraffer.approval.domain.repository.ExternalSheetLinkRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetBudgetSheetSettingsService implements GetBudgetSheetSettingsUseCase {

    private final ExternalSheetLinkRepository externalSheetLinkRepository;
    private final ObjectMapper objectMapper;

    @Override
    public BudgetSheetSettingsResult getSettings() {
        ExternalSheetLink externalSheetLink =
                externalSheetLinkRepository.findByDomain(
                                ExternalSheetLink.budgetDomain()
                        )
                        .orElseThrow(() -> new BusinessException(
                                ErrorCode.RESOURCE_NOT_FOUND,
                                "저장된 예산 시트 설정을 찾을 수 없습니다."
                        ));

        return new BudgetSheetSettingsResult(
                externalSheetLink.getSheetUrl(),
                externalSheetLink.getTabName(),
                parseColumnMapping(
                        externalSheetLink.getColumnMapping()
                ),
                externalSheetLink.getLastSyncedAt()
        );
    }

    private BudgetColumnMapping parseColumnMapping(
            String columnMapping
    ) {
        if (columnMapping == null || columnMapping.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "저장된 예산 컬럼 매핑 정보가 올바르지 않습니다."
            );
        }

        try {
            BudgetColumnMapping parsedColumnMapping = objectMapper.readValue(
                    columnMapping,
                    BudgetColumnMapping.class
            );

            if (parsedColumnMapping == null
                    || isBlank(
                    parsedColumnMapping.category()
            )
                    || isBlank(
                    parsedColumnMapping.totalAmount()
            )
                    || isBlank(
                    parsedColumnMapping.usedAmount()
            )
                    || isBlank(
                    parsedColumnMapping.remainingAmount()
            )) {
                throw new BusinessException(
                        ErrorCode.INTERNAL_SERVER_ERROR,
                        "저장된 예산 컬럼 매핑 정보가 올바르지 않습니다."
                );
            }

            return parsedColumnMapping;

        } catch (JsonProcessingException exception) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    exception
            );
        }
    }

    private boolean isBlank(
            String value
    ) {
        return value == null || value.isBlank();
    }
}
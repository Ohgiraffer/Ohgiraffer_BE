package com.ohgiraffer.submissionbox.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.submissionbox.application.command.UpdateSubmissionBoxCommand;
import com.ohgiraffer.submissionbox.application.command.UpdateSubmissionBoxItemCommand;
import com.ohgiraffer.submissionbox.application.usecase.SubmissionBoxDetailResult;
import com.ohgiraffer.submissionbox.application.usecase.UpdateSubmissionBoxUseCase;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBoxItem;
import com.ohgiraffer.submissionbox.domain.repository.SubmissionBoxRepository;
import com.ohgiraffer.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpdateSubmissionBoxService
        implements UpdateSubmissionBoxUseCase {

    private final SubmissionBoxRepository submissionBoxRepository;

    @Override
    @Transactional
    public SubmissionBoxDetailResult update(
            UpdateSubmissionBoxCommand command,
            Long requesterId,
            Role requesterRole
    ) {
        validateSubmissionBoxId(command.submissionBoxId());
        validateDuplicateItemIds(command.items());

        SubmissionBox existingSubmissionBox =
                submissionBoxRepository
                        .findByIdForUpdate(command.submissionBoxId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.SUBMISSION_BOX_NOT_FOUND
                                )
                        );

        validateManagementAuthority(
                requesterId,
                requesterRole
        );

        Map<Long, SubmissionBoxItem> existingItemMap =
                existingSubmissionBox.getItems()
                        .stream()
                        .collect(Collectors.toMap(
                                SubmissionBoxItem::getId,
                                Function.identity()
                        ));

        List<SubmissionBoxItem> updatedItems =
                command.items()
                        .stream()
                        .map(itemCommand ->
                                updateItem(
                                        itemCommand,
                                        existingItemMap
                                )
                        )
                        .toList();

        SubmissionBox updatedSubmissionBox =
                existingSubmissionBox.update(
                        command.projectName(),
                        command.targetScope(),
                        command.startAt(),
                        command.dueAt(),
                        command.latePolicy(),
                        updatedItems
                );

        SubmissionBox savedSubmissionBox =
                submissionBoxRepository.update(
                        updatedSubmissionBox
                );

        return SubmissionBoxDetailResult.from(
                savedSubmissionBox,
                LocalDateTime.now()
        );
    }

    private void validateManagementAuthority(
            Long requesterId,
            Role requesterRole
    ) {
        if (requesterId == null
                || requesterId <= 0
                || requesterRole == null) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_BOX_ACCESS_DENIED
            );
        }

        if (requesterRole != Role.MANAGER
                && requesterRole != Role.INSTRUCTOR) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_BOX_ACCESS_DENIED
            );
        }
    }

    private SubmissionBoxItem updateItem(
            UpdateSubmissionBoxItemCommand command,
            Map<Long, SubmissionBoxItem> existingItemMap
    ) {
        if (command.submissionBoxItemId() == null) {
            return SubmissionBoxItem.create(
                    command.itemName(),
                    command.itemType(),
                    command.allowedFileTypes(),
                    command.required(),
                    command.sortOrder()
            );
        }

        SubmissionBoxItem existingItem =
                existingItemMap.get(
                        command.submissionBoxItemId()
                );

        if (existingItem == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "해당 제출함에 존재하지 않는 제출 항목입니다."
            );
        }

        return existingItem.update(
                command.itemName(),
                command.itemType(),
                command.allowedFileTypes(),
                command.required(),
                command.sortOrder()
        );
    }

    private void validateSubmissionBoxId(
            Long submissionBoxId
    ) {
        if (submissionBoxId == null
                || submissionBoxId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출함 ID가 올바르지 않습니다."
            );
        }
    }

    private void validateDuplicateItemIds(
            List<UpdateSubmissionBoxItemCommand> items
    ) {
        if (items == null) {
            return;
        }

        Set<Long> itemIds = new HashSet<>();

        for (UpdateSubmissionBoxItemCommand item : items) {
            if (item.submissionBoxItemId() == null) {
                continue;
            }

            if (!itemIds.add(item.submissionBoxItemId())) {
                throw new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "동일한 제출 항목 ID를 중복해서 요청할 수 없습니다."
                );
            }
        }
    }
}
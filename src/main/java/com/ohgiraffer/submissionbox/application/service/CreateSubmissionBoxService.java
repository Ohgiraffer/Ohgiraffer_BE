package com.ohgiraffer.submissionbox.application.service;

import com.ohgiraffer.submissionbox.application.command.CreateSubmissionBoxCommand;
import com.ohgiraffer.submissionbox.application.command.CreateSubmissionBoxItemCommand;
import com.ohgiraffer.submissionbox.application.usecase.CreateSubmissionBoxResult;
import com.ohgiraffer.submissionbox.application.usecase.CreateSubmissionBoxUseCase;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBoxItem;
import com.ohgiraffer.submissionbox.domain.repository.SubmissionBoxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateSubmissionBoxService
        implements CreateSubmissionBoxUseCase {

    private final SubmissionBoxRepository submissionBoxRepository;

    @Override
    @Transactional
    public CreateSubmissionBoxResult create(
            CreateSubmissionBoxCommand command
    ) {
        List<SubmissionBoxItem> items = command.items()
                .stream()
                .map(this::createItem)
                .toList();

        SubmissionBox submissionBox = SubmissionBox.create(
                command.projectName(),
                command.targetScope(),
                command.startAt(),
                command.dueAt(),
                command.latePolicy(),
                command.createdBy(),
                items
        );

        SubmissionBox savedSubmissionBox =
                submissionBoxRepository.save(submissionBox);

        return CreateSubmissionBoxResult.from(savedSubmissionBox);
    }

    private SubmissionBoxItem createItem(
            CreateSubmissionBoxItemCommand command
    ) {
        return SubmissionBoxItem.create(
                command.itemName(),
                command.itemType(),
                command.allowedFileTypes(),
                command.required(),
                command.sortOrder()
        );
    }
}
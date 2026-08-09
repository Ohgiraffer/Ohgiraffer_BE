package com.ohgiraffer.submission.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.submission.domain.model.Submission;
import com.ohgiraffer.submission.domain.repository.SubmissionRepository;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;
import com.ohgiraffer.submissionbox.domain.model.SubmissionItemType;
import com.ohgiraffer.submissionbox.domain.repository.SubmissionBoxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SubmissionPersistenceService {

    private final SubmissionRepository submissionRepository;
    private final SubmissionBoxRepository submissionBoxRepository;

    @Transactional
    public Submission save(
            Submission submission,
            SubmissionBox expectedSubmissionBox
    ) {
        SubmissionBox currentSubmissionBox =
                submissionBoxRepository
                        .findByIdForUpdate(
                                submission.getSubmissionBoxId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.SUBMISSION_BOX_NOT_FOUND
                                )
                        );

       validateSameSubmissionBox(
        submission,
        expectedSubmissionBox,
        currentSubmissionBox
);

        validateStructureUnchanged(
        expectedSubmissionBox,
        currentSubmissionBox
        );

        return submissionRepository.save(submission);
    }

    private void validateSameSubmissionBox(
        Submission submission,
        SubmissionBox expectedSubmissionBox,
        SubmissionBox currentSubmissionBox
) {
    Long submissionBoxId =
            submission.getSubmissionBoxId();

    boolean sameExpectedSubmissionBox =
            Objects.equals(
                    submissionBoxId,
                    expectedSubmissionBox.getId()
            );

    boolean sameCurrentSubmissionBox =
            Objects.equals(
                    submissionBoxId,
                    currentSubmissionBox.getId()
            );

    if (!sameExpectedSubmissionBox
            || !sameCurrentSubmissionBox) {
        throw new BusinessException(
                ErrorCode.SUBMISSION_ITEM_MISMATCH
        );
    }
}

    private void validateStructureUnchanged(
            SubmissionBox expectedSubmissionBox,
            SubmissionBox currentSubmissionBox
    ) {
        if (expectedSubmissionBox.getTargetScope()
                != currentSubmissionBox.getTargetScope()) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH
            );
        }

        Map<Long, SubmissionItemType> expectedItemTypes =
                createItemTypeMap(expectedSubmissionBox);

        Map<Long, SubmissionItemType> currentItemTypes =
                createItemTypeMap(currentSubmissionBox);

        if (!expectedItemTypes.equals(currentItemTypes)) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH
            );
        }
    }

    private Map<Long, SubmissionItemType> createItemTypeMap(
            SubmissionBox submissionBox
    ) {
        return submissionBox.getItems()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        item -> item.getId(),
                        item -> item.getItemType()
                ));
    }
}

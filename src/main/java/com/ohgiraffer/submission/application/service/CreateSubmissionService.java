package com.ohgiraffer.submission.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.s3.S3FileHandler;
import com.ohgiraffer.global.s3.S3KeyGenerator;
import com.ohgiraffer.submission.application.command.CreateSubmissionCommand;
import com.ohgiraffer.submission.application.command.CreateSubmissionItemCommand;
import com.ohgiraffer.submission.application.usecase.CreateSubmissionResult;
import com.ohgiraffer.submission.application.usecase.CreateSubmissionUseCase;
import com.ohgiraffer.submission.domain.model.Submission;
import com.ohgiraffer.submission.domain.model.SubmissionItemValue;
import com.ohgiraffer.submission.domain.repository.StudentTeamRepository;
import com.ohgiraffer.submission.domain.repository.SubmissionRepository;
import com.ohgiraffer.submissionbox.domain.model.*;
import com.ohgiraffer.submissionbox.domain.repository.SubmissionBoxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CreateSubmissionService
        implements CreateSubmissionUseCase {

    private final SubmissionBoxRepository submissionBoxRepository;
    private final SubmissionRepository submissionRepository;
    private final StudentTeamRepository studentTeamRepository;
    private final S3FileHandler s3FileHandler;

    @Override
    @Transactional
    public CreateSubmissionResult create(
            CreateSubmissionCommand command,
            List<MultipartFile> files
    ) {
        validateCommand(command);

        SubmissionBox submissionBox =
                submissionBoxRepository
                        .findById(command.submissionBoxId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.SUBMISSION_BOX_NOT_FOUND
                                )
                        );

        LocalDateTime now = LocalDateTime.now();

        validateSubmissionPeriod(submissionBox, now);

        Owner owner = resolveOwner(
                submissionBox,
                command.submittedBy()
        );

        validateDuplicateSubmission(
                submissionBox,
                owner
        );

        Map<Long, CreateSubmissionItemCommand> commandItemMap =
                createCommandItemMap(command.items());

        validateRequiredItems(
                submissionBox,
                commandItemMap
        );

        List<String> uploadedKeys = new ArrayList<>();

        try {
            List<SubmissionItemValue> itemValues =
                    createItemValues(
                            submissionBox,
                            commandItemMap,
                            files == null
                                    ? List.of()
                                    : files,
                            command.submittedBy(),
                            uploadedKeys
                    );

            boolean late =
                    now.isAfter(submissionBox.getDueAt());

            Submission submission = Submission.create(
                    submissionBox.getId(),
                    owner.ownerUserId(),
                    owner.teamId(),
                    command.submittedBy(),
                    now,
                    late,
                    itemValues
            );

            Submission saved =
                    submissionRepository.save(submission);

            return CreateSubmissionResult.from(saved);
        } catch (RuntimeException exception) {
            deleteUploadedFiles(
                    uploadedKeys,
                    exception
            );

            throw exception;
        }
    }

    private void validateCommand(
            CreateSubmissionCommand command
    ) {
        if (command == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출 요청이 필요합니다."
            );
        }

        if (command.submissionBoxId() == null
                || command.submissionBoxId() <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출함 ID가 올바르지 않습니다."
            );
        }

        if (command.submittedBy() == null
                || command.submittedBy() <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출자 ID가 올바르지 않습니다."
            );
        }

        if (command.items() == null
                || command.items().isEmpty()) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH
            );
        }
    }

    private void validateSubmissionPeriod(
            SubmissionBox submissionBox,
            LocalDateTime now
    ) {
        if (now.isBefore(submissionBox.getStartAt())) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_NOT_STARTED
            );
        }

        if (now.isAfter(submissionBox.getDueAt())
                && submissionBox.getLatePolicy()
                == LatePolicy.BLOCK) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_DEADLINE_EXPIRED
            );
        }
    }

    private Owner resolveOwner(
            SubmissionBox submissionBox,
            Long submittedBy
    ) {
        if (submissionBox.getTargetScope()
                == SubmissionTargetScope.INDIVIDUAL) {
            return new Owner(submittedBy, null);
        }

        Long teamId = studentTeamRepository
                .findActiveTeamIdByStudentId(submittedBy)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.SUBMISSION_TEAM_NOT_FOUND
                        )
                );

        return new Owner(null, teamId);
    }

    private void validateDuplicateSubmission(
            SubmissionBox submissionBox,
            Owner owner
    ) {
        boolean duplicate;

        if (owner.ownerUserId() != null) {
            duplicate =
                    submissionRepository
                            .existsBySubmissionBoxIdAndOwnerUserId(
                                    submissionBox.getId(),
                                    owner.ownerUserId()
                            );
        } else {
            duplicate =
                    submissionRepository
                            .existsBySubmissionBoxIdAndTeamId(
                                    submissionBox.getId(),
                                    owner.teamId()
                            );
        }

        if (duplicate) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ALREADY_EXISTS
            );
        }
    }

    private Map<Long, CreateSubmissionItemCommand>
    createCommandItemMap(
            List<CreateSubmissionItemCommand> items
    ) {
        Map<Long, CreateSubmissionItemCommand> result =
                new HashMap<>();

        for (CreateSubmissionItemCommand item : items) {
            if (item == null
                    || item.submissionBoxItemId() == null) {
                throw new BusinessException(
                        ErrorCode.SUBMISSION_ITEM_MISMATCH
                );
            }

            if (result.put(
                    item.submissionBoxItemId(),
                    item
            ) != null) {
                throw new BusinessException(
                        ErrorCode.SUBMISSION_ITEM_MISMATCH,
                        "동일한 제출 항목을 중복 입력할 수 없습니다."
                );
            }
        }

        return result;
    }

    private void validateRequiredItems(
            SubmissionBox submissionBox,
            Map<Long, CreateSubmissionItemCommand> itemMap
    ) {
        Set<Long> boxItemIds = submissionBox.getItems()
                .stream()
                .map(SubmissionBoxItem::getId)
                .collect(java.util.stream.Collectors.toSet());

        if (!boxItemIds.containsAll(itemMap.keySet())) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH
            );
        }

        boolean missingRequiredItem =
                submissionBox.getItems()
                        .stream()
                        .filter(SubmissionBoxItem::isRequired)
                        .map(SubmissionBoxItem::getId)
                        .anyMatch(id -> !itemMap.containsKey(id));

        if (missingRequiredItem) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH,
                    "필수 제출 항목이 누락되었습니다."
            );
        }
    }

    private List<SubmissionItemValue> createItemValues(
            SubmissionBox submissionBox,
            Map<Long, CreateSubmissionItemCommand> commandItemMap,
            List<MultipartFile> files,
            Long submittedBy,
            List<String> uploadedKeys
    ) {
        List<SubmissionItemValue> values =
                new ArrayList<>();

        for (SubmissionBoxItem boxItem
                : submissionBox.getItems()) {
            CreateSubmissionItemCommand itemCommand =
                    commandItemMap.get(boxItem.getId());

            if (itemCommand == null) {
                continue;
            }

            if (boxItem.getItemType()
                    == SubmissionItemType.FILE) {
                values.add(
                        createFileValue(
                                submissionBox,
                                boxItem,
                                itemCommand,
                                files,
                                submittedBy,
                                uploadedKeys
                        )
                );
            } else {
                values.add(
                        createLinkValue(
                                boxItem,
                                itemCommand
                        )
                );
            }
        }

        if (values.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH
            );
        }

        return values;
    }

    private SubmissionItemValue createFileValue(
            SubmissionBox submissionBox,
            SubmissionBoxItem boxItem,
            CreateSubmissionItemCommand command,
            List<MultipartFile> files,
            Long submittedBy,
            List<String> uploadedKeys
    ) {
        if (command.externalUrl() != null
                && !command.externalUrl().isBlank()) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH,
                    "파일 항목에는 외부 링크를 입력할 수 없습니다."
            );
        }

        Integer fileIndex = command.fileIndex();

        if (fileIndex == null
                || fileIndex < 0
                || fileIndex >= files.size()) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH,
                    "제출 파일을 찾을 수 없습니다."
            );
        }

        MultipartFile file = files.get(fileIndex);

        if (file == null || file.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "빈 파일은 제출할 수 없습니다."
            );
        }

        validateAllowedFileType(
                boxItem.getAllowedFileTypes(),
                file.getOriginalFilename()
        );

        String key = S3KeyGenerator.submissionFileKey(
                submissionBox.getId(),
                submittedBy,
                file.getOriginalFilename()
        );

        s3FileHandler.upload(file, key);
        uploadedKeys.add(key);

        return SubmissionItemValue.createFile(
                boxItem.getId(),
                key,
                Objects.requireNonNullElse(
                        file.getOriginalFilename(),
                        "file"
                ),
                file.getContentType(),
                file.getSize()
        );
    }

    private SubmissionItemValue createLinkValue(
            SubmissionBoxItem boxItem,
            CreateSubmissionItemCommand command
    ) {
        if (command.fileIndex() != null) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH,
                    "외부 링크 항목에는 파일을 입력할 수 없습니다."
            );
        }

        validateExternalUrl(command.externalUrl());

        return SubmissionItemValue.createLink(
                boxItem.getId(),
                command.externalUrl()
        );
    }

    private void validateAllowedFileType(
            String allowedFileTypes,
            String originalFileName
    ) {
        if (originalFileName == null
                || originalFileName.isBlank()) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_FILE_TYPE_NOT_ALLOWED
            );
        }

        int extensionIndex =
                originalFileName.lastIndexOf('.');

        if (extensionIndex < 0
                || extensionIndex
                == originalFileName.length() - 1) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_FILE_TYPE_NOT_ALLOWED
            );
        }

        String extension =
                originalFileName
                        .substring(extensionIndex + 1)
                        .toLowerCase(Locale.ROOT);

        Set<String> allowedExtensions =
                Arrays.stream(
                                allowedFileTypes.split("[,\\s]+")
                        )
                        .map(String::trim)
                        .map(value ->
                                value.startsWith(".")
                                        ? value.substring(1)
                                        : value
                        )
                        .map(value ->
                                value.toLowerCase(Locale.ROOT)
                        )
                        .filter(value -> !value.isBlank())
                        .collect(java.util.stream.Collectors.toSet());

        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_FILE_TYPE_NOT_ALLOWED
            );
        }
    }

    private void validateExternalUrl(
            String externalUrl
    ) {
        if (externalUrl == null
                || externalUrl.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "외부 링크가 필요합니다."
            );
        }

        try {
            URI uri = new URI(externalUrl.trim());

            String scheme = uri.getScheme();

            if (scheme == null
                    || (!scheme.equalsIgnoreCase("http")
                    && !scheme.equalsIgnoreCase("https"))
                    || uri.getHost() == null) {
                throw new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "올바른 HTTP 또는 HTTPS URL을 입력해주세요."
                );
            }
        } catch (URISyntaxException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "외부 링크 형식이 올바르지 않습니다."
            );
        }
    }

    private void deleteUploadedFiles(
            List<String> uploadedKeys,
            RuntimeException originalException
    ) {
        for (String key : uploadedKeys) {
            try {
                s3FileHandler.delete(key);
            } catch (RuntimeException deleteException) {
                originalException.addSuppressed(
                        deleteException
                );
            }
        }
    }

    private record Owner(
            Long ownerUserId,
            Long teamId
    ) {
    }
}
package com.ohgiraffer.submission.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.s3.S3FileHandler;
import com.ohgiraffer.global.s3.S3KeyGenerator;
import com.ohgiraffer.submission.application.command.UpdateSubmissionCommand;
import com.ohgiraffer.submission.application.command.UpdateSubmissionItemCommand;
import com.ohgiraffer.submission.application.usecase.UpdateSubmissionResult;
import com.ohgiraffer.submission.application.usecase.UpdateSubmissionUseCase;
import com.ohgiraffer.submission.domain.model.Submission;
import com.ohgiraffer.submission.domain.model.SubmissionItemValue;
import com.ohgiraffer.submission.domain.repository.StudentTeamRepository;
import com.ohgiraffer.submission.domain.repository.SubmissionRepository;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBoxItem;
import com.ohgiraffer.submissionbox.domain.model.SubmissionItemType;
import com.ohgiraffer.submissionbox.domain.repository.SubmissionBoxRepository;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.model.UserStatus;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateSubmissionService
        implements UpdateSubmissionUseCase {

    private static final int MAX_FILE_COUNT = 10;

    private static final long MAX_SINGLE_FILE_SIZE =
            100L * 1024 * 1024;

    private static final long MAX_TOTAL_FILE_SIZE =
            110L * 1024 * 1024;

    private static final int MAX_ORIGINAL_FILE_NAME_LENGTH =
            255;

    private final SubmissionRepository
            submissionRepository;

    private final SubmissionBoxRepository
            submissionBoxRepository;

    private final StudentTeamRepository
            studentTeamRepository;

    private final UserRepository
            userRepository;

    private final SubmissionPersistenceService
            persistenceService;

    private final S3FileHandler
            s3FileHandler;

    @Override
    public UpdateSubmissionResult update(
            UpdateSubmissionCommand command,
            List<MultipartFile> files
    ) {
        validateCommand(command);

        List<MultipartFile> safeFiles =
                files == null
                        ? List.of()
                        : files;

        validateFiles(safeFiles);

        Submission existingSubmission =
                submissionRepository
                        .findById(command.submissionId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.SUBMISSION_NOT_FOUND
                                )
                        );

        SubmissionBox submissionBox =
                submissionBoxRepository
                        .findById(
                                existingSubmission
                                        .getSubmissionBoxId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode
                                                .SUBMISSION_BOX_NOT_FOUND
                                )
                        );

        validateStudent(command.requestedBy());

        validateOwnership(
                existingSubmission,
                command.requestedBy()
        );

        LocalDateTime resubmittedAt =
                LocalDateTime.now();

        validateModificationPeriod(
                submissionBox,
                resubmittedAt
        );

        Map<Long, UpdateSubmissionItemCommand>
                commandItemMap =
                createCommandItemMap(
                        command.items()
                );

        validateRequiredItems(
                submissionBox,
                commandItemMap
        );

        List<String> oldFileKeys =
                extractFileKeys(
                        existingSubmission
                                .getItemValues()
                );

        List<String> uploadedKeys =
                new ArrayList<>();

        Submission savedSubmission;

        try {
            List<SubmissionItemValue> newItemValues =
                    createItemValues(
                            submissionBox,
                            commandItemMap,
                            safeFiles,
                            command.requestedBy(),
                            uploadedKeys
                    );

            Submission updatedSubmission =
                    existingSubmission.resubmit(
                            command.requestedBy(),
                            resubmittedAt,
                            newItemValues
                    );

            savedSubmission =
                    persistenceService.save(
                            updatedSubmission
                    );
        } catch (RuntimeException exception) {
            deleteNewFilesAfterFailure(
                    uploadedKeys,
                    exception
            );

            throw exception;
        }

        UpdateSubmissionResult result =
                UpdateSubmissionResult.from(
                        savedSubmission
                );

        deleteOldFilesBestEffort(
                oldFileKeys
        );

        return result;
    }

    private void validateCommand(
            UpdateSubmissionCommand command
    ) {
        if (command == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출물 수정 요청이 필요합니다."
            );
        }

        if (command.submissionId() == null
                || command.submissionId() <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출물 ID가 올바르지 않습니다."
            );
        }

        if (command.requestedBy() == null
                || command.requestedBy() <= 0) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ACCESS_DENIED
            );
        }

        if (command.items() == null
                || command.items().isEmpty()) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH
            );
        }
    }

    private void validateStudent(
            Long studentId
    ) {
        User student =
                userRepository
                        .findById(studentId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.USER_NOT_FOUND
                                )
                        );

        if (student.getRole() != Role.STUDENT
                || student.getStatus()
                != UserStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ACCESS_DENIED
            );
        }
    }

    private void validateOwnership(
            Submission submission,
            Long requestedBy
    ) {
        if (submission.getOwnerUserId() != null) {
            if (!requestedBy.equals(
                    submission.getOwnerUserId()
            )) {
                throw new BusinessException(
                        ErrorCode.SUBMISSION_ACCESS_DENIED
                );
            }

            return;
        }

        Long submissionTeamId =
                submission.getTeamId();

        if (submissionTeamId == null) {
            throw new BusinessException(
                    ErrorCode
                            .SUBMISSION_TEAM_DATA_INCONSISTENT
            );
        }

        Long requesterTeamId =
                studentTeamRepository
                        .findActiveTeamIdByStudentId(
                                requestedBy
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode
                                                .SUBMISSION_TEAM_NOT_FOUND
                                )
                        );

        if (!submissionTeamId.equals(
                requesterTeamId
        )) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ACCESS_DENIED
            );
        }
    }

    private void validateModificationPeriod(
            SubmissionBox submissionBox,
            LocalDateTime now
    ) {
        if (now.isBefore(
                submissionBox.getStartAt()
        )) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_NOT_STARTED
            );
        }

        if (now.isAfter(
                submissionBox.getDueAt()
        )) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_DEADLINE_EXPIRED
            );
        }
    }

    private void validateFiles(
            List<MultipartFile> files
    ) {
        if (files.size() > MAX_FILE_COUNT) {
            throw new BusinessException(
                    ErrorCode
                            .SUBMISSION_FILE_COUNT_EXCEEDED
            );
        }

        long totalSize = 0L;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "빈 파일은 제출할 수 없습니다."
                );
            }

            String originalFileName =
                    file.getOriginalFilename();

            if (originalFileName == null
                    || originalFileName.isBlank()) {
                throw new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "파일명이 없는 파일은 제출할 수 없습니다."
                );
            }

            if (originalFileName.trim().length()
                    > MAX_ORIGINAL_FILE_NAME_LENGTH) {
                throw new BusinessException(
                        ErrorCode
                                .SUBMISSION_FILE_NAME_TOO_LONG
                );
            }

            long fileSize =
                    file.getSize();

            if (fileSize <= 0) {
                throw new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "빈 파일은 제출할 수 없습니다."
                );
            }

            if (fileSize > MAX_SINGLE_FILE_SIZE) {
                throw new BusinessException(
                        ErrorCode
                                .SUBMISSION_FILE_TOO_LARGE,
                        "파일 한 개의 크기는 100MB를 초과할 수 없습니다."
                );
            }

            if (totalSize
                    > MAX_TOTAL_FILE_SIZE - fileSize) {
                throw new BusinessException(
                        ErrorCode
                                .SUBMISSION_FILE_TOO_LARGE,
                        "전체 파일 크기는 110MB를 초과할 수 없습니다."
                );
            }

            totalSize += fileSize;
        }
    }

    private Map<Long, UpdateSubmissionItemCommand>
    createCommandItemMap(
            List<UpdateSubmissionItemCommand> items
    ) {
        Map<Long, UpdateSubmissionItemCommand> result =
                new HashMap<>();

        for (UpdateSubmissionItemCommand item : items) {
            if (item == null
                    || item.submissionBoxItemId() == null
                    || item.submissionBoxItemId() <= 0) {
                throw new BusinessException(
                        ErrorCode.SUBMISSION_ITEM_MISMATCH
                );
            }

            UpdateSubmissionItemCommand previous =
                    result.put(
                            item.submissionBoxItemId(),
                            item
                    );

            if (previous != null) {
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
            Map<Long, UpdateSubmissionItemCommand>
                    itemMap
    ) {
        Set<Long> boxItemIds =
                submissionBox.getItems()
                        .stream()
                        .map(SubmissionBoxItem::getId)
                        .collect(Collectors.toSet());

        if (!boxItemIds.containsAll(
                itemMap.keySet()
        )) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH
            );
        }

        boolean missingRequiredItem =
                submissionBox.getItems()
                        .stream()
                        .filter(
                                SubmissionBoxItem::isRequired
                        )
                        .map(SubmissionBoxItem::getId)
                        .anyMatch(id ->
                                !itemMap.containsKey(id)
                        );

        if (missingRequiredItem) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH,
                    "필수 제출 항목이 누락되었습니다."
            );
        }
    }

    private List<SubmissionItemValue>
    createItemValues(
            SubmissionBox submissionBox,
            Map<Long, UpdateSubmissionItemCommand>
                    commandItemMap,
            List<MultipartFile> files,
            Long requestedBy,
            List<String> uploadedKeys
    ) {
        List<SubmissionItemValue> values =
                new ArrayList<>();

        Set<Integer> usedFileIndexes =
                new HashSet<>();

        for (SubmissionBoxItem boxItem
                : submissionBox.getItems()) {
            UpdateSubmissionItemCommand itemCommand =
                    commandItemMap.get(
                            boxItem.getId()
                    );

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
                                requestedBy,
                                uploadedKeys,
                                usedFileIndexes
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

        if (usedFileIndexes.size()
                != files.size()) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH,
                    "제출 항목과 연결되지 않은 파일이 존재합니다."
            );
        }

        return values;
    }

    private SubmissionItemValue createFileValue(
            SubmissionBox submissionBox,
            SubmissionBoxItem boxItem,
            UpdateSubmissionItemCommand command,
            List<MultipartFile> files,
            Long requestedBy,
            List<String> uploadedKeys,
            Set<Integer> usedFileIndexes
    ) {
        if (command.externalUrl() != null
                && !command.externalUrl().isBlank()) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH,
                    "파일 항목에는 외부 링크를 입력할 수 없습니다."
            );
        }

        Integer fileIndex =
                command.fileIndex();

        if (fileIndex == null
                || fileIndex < 0
                || fileIndex >= files.size()) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH,
                    "제출 파일을 찾을 수 없습니다."
            );
        }

        if (!usedFileIndexes.add(fileIndex)) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH,
                    "동일한 파일 인덱스를 중복 사용할 수 없습니다."
            );
        }

        MultipartFile file =
                files.get(fileIndex);

        if (file == null || file.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "빈 파일은 제출할 수 없습니다."
            );
        }

        String originalFileName =
                Objects.requireNonNullElse(
                        file.getOriginalFilename(),
                        "file"
                ).trim();

        validateAllowedFileType(
                boxItem.getAllowedFileTypes(),
                originalFileName
        );

        String key =
                S3KeyGenerator.submissionFileKey(
                        submissionBox.getId(),
                        requestedBy,
                        originalFileName
                );

        if (key.length() > 500) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "파일 저장 키는 500자 이하여야 합니다."
            );
        }

        s3FileHandler.upload(
                file,
                key
        );

        uploadedKeys.add(key);

        return SubmissionItemValue.createFile(
                boxItem.getId(),
                key,
                originalFileName,
                file.getContentType(),
                file.getSize()
        );
    }

    private SubmissionItemValue createLinkValue(
            SubmissionBoxItem boxItem,
            UpdateSubmissionItemCommand command
    ) {
        if (command.fileIndex() != null) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH,
                    "외부 링크 항목에는 파일을 입력할 수 없습니다."
            );
        }

        validateExternalUrl(
                command.externalUrl()
        );

        return SubmissionItemValue.createLink(
                boxItem.getId(),
                command.externalUrl().trim()
        );
    }

    private void validateAllowedFileType(
            String allowedFileTypes,
            String originalFileName
    ) {
        if (originalFileName == null
                || originalFileName.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "파일명이 필요합니다."
            );
        }

        if (allowedFileTypes == null
                || allowedFileTypes.isBlank()) {
            throw new BusinessException(
                    ErrorCode
                            .SUBMISSION_FILE_TYPE_NOT_ALLOWED
            );
        }

        Set<String> allowedExtensions =
                Arrays.stream(
                                allowedFileTypes.split(",")
                        )
                        .map(String::trim)
                        .map(value ->
                                value.toLowerCase(
                                        Locale.ROOT
                                )
                        )
                        .filter(value ->
                                !value.isBlank()
                        )
                        .collect(Collectors.toSet());

        if (allowedExtensions.contains("*")) {
            return;
        }

        int extensionIndex =
                originalFileName.lastIndexOf('.');

        if (extensionIndex < 0
                || extensionIndex
                == originalFileName.length() - 1) {
            throw new BusinessException(
                    ErrorCode
                            .SUBMISSION_FILE_TYPE_NOT_ALLOWED
            );
        }

        String extension =
                originalFileName
                        .substring(extensionIndex + 1)
                        .toLowerCase(Locale.ROOT);

        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException(
                    ErrorCode
                            .SUBMISSION_FILE_TYPE_NOT_ALLOWED
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

        String trimmedUrl =
                externalUrl.trim();

        if (trimmedUrl.length() > 1000) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "외부 링크는 1000자 이하여야 합니다."
            );
        }

        try {
            URI uri =
                    new URI(trimmedUrl);

            String scheme =
                    uri.getScheme();

            boolean supportedScheme =
                    scheme != null
                            && (scheme.equalsIgnoreCase("http")
                            || scheme.equalsIgnoreCase("https"));

            if (!supportedScheme
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

    private List<String> extractFileKeys(
            List<SubmissionItemValue> values
    ) {
        return values.stream()
                .map(SubmissionItemValue::getFileKey)
                .filter(Objects::nonNull)
                .filter(key ->
                        !key.isBlank()
                )
                .toList();
    }

    private void deleteNewFilesAfterFailure(
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

    private void deleteOldFilesBestEffort(
            List<String> oldFileKeys
    ) {
        for (String key : oldFileKeys) {
            try {
                s3FileHandler.delete(key);
            } catch (RuntimeException exception) {
                log.warn(
                        "기존 제출 파일 삭제 실패. key={}",
                        key,
                        exception
                );
            }
        }
    }
}
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
import com.ohgiraffer.global.metrics.CampFlowMetrics;
import io.micrometer.core.instrument.Timer;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.Clock;
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
    private static final long MAX_SINGLE_FILE_SIZE = 100L * 1024 * 1024;
    private static final long MAX_TOTAL_FILE_SIZE = 110L * 1024 * 1024;
    private static final int MAX_ORIGINAL_FILE_NAME_LENGTH = 255;

    /*
     * 재제출 과정에서 발생하는 실제 S3 업로드 시간을 기록합니다.
     * 최초 제출과 동일한 메트릭 이름을 사용하여 전체 파일 업로드
     * 성능을 하나의 Grafana 그래프에서 확인할 수 있게 합니다.
     */
    private static final String FILE_UPLOAD_DURATION_METRIC = "campflow.submission.file.upload.duration";

    /*
     * 신규 파일 보상 삭제 또는 기존 파일 교체 삭제에 실패한
     * 횟수를 기록합니다.
     */
    private static final String FILE_CLEANUP_FAILED_METRIC = "campflow.submission.file.cleanup.failed.count";

    private final SubmissionRepository submissionRepository;
    private final SubmissionBoxRepository submissionBoxRepository;
    private final StudentTeamRepository studentTeamRepository;
    private final UserRepository userRepository;
    private final SubmissionPersistenceService persistenceService;
    private final S3FileHandler s3FileHandler;
    private final CampFlowMetrics campFlowMetrics;
    private final Clock clock;

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
                submissionBox,
                command.requestedBy()
        );

        LocalDateTime resubmittedAt =
                LocalDateTime.now(clock);

        validateModificationPeriod(
                submissionBox,
                resubmittedAt
        );

        Map<Long, UpdateSubmissionItemCommand>
                commandItemMap =
                createCommandItemMap(
                        command.items()
                );

        /*
         * 수정 요청에 들어온 항목 ID가 현재 제출함에 속하는지만 확인합니다.
         *
         * 수정 요청에 포함되지 않은 필수 항목은 기존 제출값을 유지하므로,
         * 요청 자체에 모든 필수 항목이 들어올 필요는 없습니다.
         */
        validateRequestedItems(
                submissionBox,
                commandItemMap
        );

        List<String> oldFileKeys =
                extractFileKeys(
                        existingSubmission.getItemValues()
                );

        List<String> uploadedKeys =
                new ArrayList<>();

        List<String> obsoleteFileKeys =
                List.of();

        Submission savedSubmission;

        try {
            /*
             * 요청에 포함된 항목은 새 값으로 교체하고,
             * 요청에 포함되지 않은 항목은 기존 값을 유지합니다.
             */
            List<SubmissionItemValue> mergedItemValues =
                    createMergedItemValues(
                            submissionBox,
                            existingSubmission.getItemValues(),
                            commandItemMap,
                            safeFiles,
                            command.requestedBy(),
                            uploadedKeys
                    );

            /*
             * 병합된 최종 결과를 기준으로 필수 항목이 모두 존재하는지 검사합니다.
             */
            validateFinalRequiredItems(
                    submissionBox,
                    mergedItemValues
            );

            /*
             * 기존 파일 중 최종 결과에서 더 이상 참조하지 않는 파일만
             * 저장 성공 후 삭제 대상으로 지정합니다.
             *
             * 수정되지 않은 기존 파일은 mergedItemValues에 남아 있으므로
             * 삭제 대상에 포함되지 않습니다.
             */
            obsoleteFileKeys =
                    findObsoleteFileKeys(
                            oldFileKeys,
                            mergedItemValues
                    );

            Submission updatedSubmission =
                    existingSubmission.resubmit(
                            command.requestedBy(),
                            resubmittedAt,
                            mergedItemValues
                    );

            savedSubmission =
                    persistenceService.save(
                            updatedSubmission,
                            submissionBox
                    );
        } catch (RuntimeException exception) {
            /*
             * S3 업로드 이후 DB 저장이 실패한 경우,
             * 이번 수정 요청에서 새로 업로드한 파일만 보상 삭제합니다.
             */
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

        /*
         * DB 저장이 성공한 이후에 교체된 기존 파일만 삭제합니다.
         */
        deleteOldFilesBestEffort(
                obsoleteFileKeys
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
            SubmissionBox submissionBox,
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
                    ErrorCode.SUBMISSION_TEAM_DATA_INCONSISTENT
            );
        }

        Long requesterTeamId =
                studentTeamRepository
                        .findTeamIdByStudentIdAndDateTime(
                                requestedBy,
                                submissionBox.getStartAt()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.SUBMISSION_TEAM_NOT_FOUND
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

    /**
     * 수정 요청에 포함된 항목이 현재 제출함의 항목인지 확인합니다.
     *
     * 부분 수정에서는 모든 필수 항목을 다시 보낼 필요가 없습니다.
     * 요청에 없는 항목은 기존 제출값을 유지합니다.
     */
    private void validateRequestedItems(
            SubmissionBox submissionBox,
            Map<Long, UpdateSubmissionItemCommand> itemMap
    ) {
        Set<Long> boxItemIds =
                submissionBox.getItems()
                        .stream()
                        .map(SubmissionBoxItem::getId)
                        .collect(Collectors.toSet());

        if (!boxItemIds.containsAll(itemMap.keySet())) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH,
                    "현재 제출함에 존재하지 않는 제출 항목이 포함되어 있습니다."
            );
        }
    }

    /**
     * 기존 값과 수정값을 병합한 최종 결과를 기준으로
     * 필수 제출 항목이 모두 존재하는지 확인합니다.
     */
    private void validateFinalRequiredItems(
            SubmissionBox submissionBox,
            List<SubmissionItemValue> mergedValues
    ) {
        Set<Long> submittedItemIds =
                mergedValues.stream()
                        .map(
                                SubmissionItemValue
                                        ::getSubmissionBoxItemId
                        )
                        .collect(Collectors.toSet());

        boolean missingRequiredItem =
                submissionBox.getItems()
                        .stream()
                        .filter(SubmissionBoxItem::isRequired)
                        .map(SubmissionBoxItem::getId)
                        .anyMatch(id ->
                                !submittedItemIds.contains(id)
                        );

        if (missingRequiredItem) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH,
                    "필수 제출 항목이 누락되었습니다."
            );
        }
    }

    /**
     * 기존 제출값과 이번 수정 요청을 병합합니다.
     *
     * - 수정 요청에 포함된 항목: 새 값으로 교체
     * - 수정 요청에 없는 항목: 기존 값 유지
     */
    private List<SubmissionItemValue>
    createMergedItemValues(
            SubmissionBox submissionBox,
            List<SubmissionItemValue> existingValues,
            Map<Long, UpdateSubmissionItemCommand> commandItemMap,
            List<MultipartFile> files,
            Long requestedBy,
            List<String> uploadedKeys
    ) {
        Map<Long, SubmissionItemValue> existingValueMap =
                existingValues.stream()
                        .collect(
                                Collectors.toMap(
                                        SubmissionItemValue
                                                ::getSubmissionBoxItemId,
                                        value -> value
                                )
                        );

        List<SubmissionItemValue> mergedValues =
                new ArrayList<>();

        Set<Integer> usedFileIndexes =
                new HashSet<>();

        for (SubmissionBoxItem boxItem
                : submissionBox.getItems()) {
            Long submissionBoxItemId =
                    boxItem.getId();

            UpdateSubmissionItemCommand itemCommand =
                    commandItemMap.get(
                            submissionBoxItemId
                    );

            /*
             * 수정 요청에 해당 항목이 없으면 기존 값을 그대로 유지합니다.
             */
            if (itemCommand == null) {
                SubmissionItemValue existingValue =
                        existingValueMap.get(
                                submissionBoxItemId
                        );

                if (existingValue != null) {
                    mergedValues.add(existingValue);
                }

                continue;
            }

            /*
             * 수정 요청에 포함된 항목만 새 값으로 생성합니다.
             */
            if (boxItem.getItemType()
                    == SubmissionItemType.FILE) {
                mergedValues.add(
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
                mergedValues.add(
                        createLinkValue(
                                boxItem,
                                itemCommand
                        )
                );
            }
        }

        if (mergedValues.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH,
                    "저장할 제출 항목이 없습니다."
            );
        }

        /*
         * 전송된 파일 중 어떤 제출 항목에도 연결되지 않은 파일이 있으면
         * 잘못된 요청으로 처리합니다.
         */
        if (usedFileIndexes.size() != files.size()) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH,
                    "제출 항목과 연결되지 않은 파일이 존재합니다."
            );
        }

        return mergedValues;
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

        /*
         * 재제출 메서드 전체 시간이 아닌 실제 S3 업로드 구간을
         * 별도로 측정합니다.
         *
         * 업로드 중 예외가 발생해도 소요 시간은 남겨야 하므로
         * finally에서 Timer를 종료합니다.
         */
        Timer.Sample uploadSample =
                campFlowMetrics.startTimer();

        try {
            s3FileHandler.upload(
                    file,
                    key
            );
        } finally {
            campFlowMetrics.stopTimer(
                    uploadSample,
                    FILE_UPLOAD_DURATION_METRIC
            );
        }

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

    /**
     * 기존 파일 중 수정 이후 더 이상 제출물에서 참조하지 않는 파일을 찾습니다.
     *
     * 수정하지 않은 파일은 최종 제출값에 그대로 남으므로 삭제되지 않습니다.
     */
    private List<String> findObsoleteFileKeys(
            List<String> oldFileKeys,
            List<SubmissionItemValue> mergedValues
    ) {
        Set<String> retainedFileKeys =
                extractFileKeys(mergedValues)
                        .stream()
                        .collect(Collectors.toSet());

        return oldFileKeys.stream()
                .filter(key ->
                        !retainedFileKeys.contains(key)
                )
                .distinct()
                .toList();
    }

    /**
     * 재제출 중 새로 업로드한 파일을 보상 삭제합니다.
     *
     * 새 파일 업로드 이후 DB 저장 등에 실패한 경우 호출됩니다.
     */
    private void deleteNewFilesAfterFailure(
            List<String> uploadedKeys,
            RuntimeException originalException
    ) {
        for (String key : uploadedKeys) {
            try {
                s3FileHandler.delete(key);
            } catch (RuntimeException deleteException) {
                /*
                 * 재제출 요청 실패 후 신규 파일을 되돌리는 과정에서
                 * 발생한 정리 실패이므로 rollback으로 구분합니다.
                 */
                campFlowMetrics.incrementCounter(
                        FILE_CLEANUP_FAILED_METRIC,
                        "phase", "rollback"
                );

                originalException.addSuppressed(
                        deleteException
                );
            }
        }
    }

    /**
     * 재제출이 정상적으로 저장된 이후 기존 제출 파일을 삭제합니다.
     *
     * 새 제출 데이터는 이미 정상 저장되었으므로 기존 파일 삭제가
     * 실패해도 재제출 결과 자체는 실패시키지 않습니다.
     */
    private void deleteOldFilesBestEffort(
            List<String> oldFileKeys
    ) {
        for (String key : oldFileKeys) {
            try {
                s3FileHandler.delete(key);
            } catch (RuntimeException exception) {
                /*
                 * 기존 파일을 새 파일로 교체한 뒤 발생한 삭제 실패이므로
                 * replace 태그로 구분합니다.
                 *
                 * 이 값이 증가하면 더 이상 사용되지 않는 기존 파일이
                 * S3에 남아 있을 가능성이 있습니다.
                 */
                campFlowMetrics.incrementCounter(
                        FILE_CLEANUP_FAILED_METRIC,
                        "phase", "replace"
                );

                log.warn(
                        "기존 제출 파일 삭제 실패. key={}",
                        key,
                        exception
                );
            }
        }
    }

}
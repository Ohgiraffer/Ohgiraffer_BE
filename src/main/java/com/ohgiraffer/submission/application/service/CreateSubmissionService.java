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
import com.ohgiraffer.submissionbox.domain.model.LatePolicy;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBoxItem;
import com.ohgiraffer.submissionbox.domain.model.SubmissionItemType;
import com.ohgiraffer.submissionbox.domain.model.SubmissionTargetScope;
import com.ohgiraffer.submissionbox.domain.repository.SubmissionBoxRepository;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.model.UserStatus;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
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

@Service
@RequiredArgsConstructor
public class CreateSubmissionService
        implements CreateSubmissionUseCase {

    private static final int MAX_FILE_COUNT = 10;
    private static final long MAX_SINGLE_FILE_SIZE = 100L * 1024 * 1024;
    private static final long MAX_TOTAL_FILE_SIZE = 110L * 1024 * 1024;
    private static final int MAX_ORIGINAL_FILE_NAME_LENGTH = 255;
    private final SubmissionBoxRepository submissionBoxRepository;
    private final Clock clock;

    /*
     * 제출 전 중복 확인에 사용합니다.
     *
     * 최종 중복 방지는 DB 유니크 제약과
     * SubmissionRepositoryAdapter의 예외 변환이 담당합니다.
     */
    private final SubmissionRepository
            submissionRepository;

    private final StudentTeamRepository
            studentTeamRepository;

    private final UserRepository
            userRepository;

    /*
     * 실제 DB 저장과 트랜잭션 커밋을 담당하는 별도 서비스입니다.
     *
     * CreateSubmissionService 자체에는 @Transactional을 붙이지 않습니다.
     * 그래야 S3 업로드 중 DB 트랜잭션과 커넥션을 점유하지 않습니다.
     */
    private final SubmissionPersistenceService
            persistenceService;

    private final S3FileHandler
            s3FileHandler;

    @Override
    public CreateSubmissionResult create(
            CreateSubmissionCommand command,
            List<MultipartFile> files
    ) {
        validateCommand(command);

        List<MultipartFile> safeFiles =
                files == null
                        ? List.of()
                        : files;

        validateFiles(safeFiles);

        SubmissionBox submissionBox =
                submissionBoxRepository
                        .findById(
                                command.submissionBoxId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode
                                                .SUBMISSION_BOX_NOT_FOUND
                                )
                        );

        validateSubmissionAccess(
                command.submittedBy()
        );

        LocalDateTime submittedAt =
                LocalDateTime.now(clock);

        validateSubmissionPeriod(
                submissionBox,
                submittedAt
        );

        Owner owner = resolveOwner(
                submissionBox,
                command.submittedBy()
        );

        /*
         * 빠른 중복 응답을 위한 사전 검사입니다.
         *
         * 동시에 요청이 들어오는 경쟁 상태의 최종 방지는
         * DB 유니크 제약으로 처리됩니다.
         */
        validateDuplicateSubmission(
                submissionBox,
                owner
        );

        Map<Long, CreateSubmissionItemCommand>
                commandItemMap =
                createCommandItemMap(
                        command.items()
                );

        validateRequiredItems(
                submissionBox,
                commandItemMap
        );

        List<String> uploadedKeys =
                new ArrayList<>();

        try {
            List<SubmissionItemValue> itemValues =
                    createItemValues(
                            submissionBox,
                            commandItemMap,
                            safeFiles,
                            command.submittedBy(),
                            uploadedKeys
                    );

            boolean late =
                    submittedAt.isAfter(
                            submissionBox.getDueAt()
                    );

            Submission submission =
                    Submission.create(
                            submissionBox.getId(),
                            owner.ownerUserId(),
                            owner.teamId(),
                            command.submittedBy(),
                            submittedAt,
                            late,
                            itemValues
                    );

            /*
             * 별도 Spring Bean을 호출하기 때문에
             * SubmissionPersistenceService의 @Transactional이
             * 정상적으로 적용됩니다.
             *
             * DB 커밋 실패도 이 호출에서 발생하므로
             * 바깥 catch에서 S3 보상 삭제를 수행할 수 있습니다.
             */
            Submission saved =
                    persistenceService.save(
                            submission
                    );

            return CreateSubmissionResult.from(
                    saved
            );
        } catch (RuntimeException exception) {
            /*
             * 파일 업로드 이후 어떤 단계에서든 실패하면
             * 이번 요청에서 올린 S3 파일을 삭제합니다.
             */
            deleteUploadedFiles(
                    uploadedKeys,
                    exception
            );

            throw exception;
        }
    }

    /**
     * 기본 요청값을 검증합니다.
     */
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

    /**
     * 파일 개수, 단일 파일 크기, 전체 파일 크기,
     * 원본 파일명 길이를 검증합니다.
     */
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

            String trimmedFileName =
                    originalFileName.trim();

            if (trimmedFileName.length()
                    > MAX_ORIGINAL_FILE_NAME_LENGTH) {
                throw new BusinessException(
                        ErrorCode
                                .SUBMISSION_FILE_NAME_TOO_LONG
                );
            }

            long fileSize = file.getSize();

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

            /*
             * totalSize + fileSize 방식은 long 오버플로가
             * 발생할 수 있으므로 뺄셈으로 비교합니다.
             */
            if (totalSize
                    > MAX_TOTAL_FILE_SIZE - fileSize) {
                throw new BusinessException(
                        ErrorCode
                                .SUBMISSION_FILE_TOO_LARGE,
                        "제출 파일의 전체 크기는 110MB를 초과할 수 없습니다."
                );
            }

            totalSize += fileSize;
        }
    }

    /**
     * 제출자가 활성 학생인지, 제출함 생성자와 같은
     * 부트캠프 소속인지 확인합니다.
     *
     * 현재 submission_box에 bootcamp_id가 없기 때문에
     * 제출함 생성자의 bootcampId를 기준으로 검사합니다.
     */
    private void validateSubmissionAccess(
            Long studentId
    ) {
        User student = userRepository
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

    /**
     * 제출 시작일과 마감일 정책을 검증합니다.
     */
    private void validateSubmissionPeriod(
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
        )
                && submissionBox.getLatePolicy()
                == LatePolicy.BLOCK) {
            throw new BusinessException(
                    ErrorCode
                            .SUBMISSION_DEADLINE_EXPIRED
            );
        }
    }

    /**
     * 개인 제출이면 학생 본인을 소유자로 사용합니다.
     *
     * 팀 제출이면 현재 활성 팀을 조회해 팀을
     * 제출물 소유자로 사용합니다.
     */
    private Owner resolveOwner(
            SubmissionBox submissionBox,
            Long submittedBy
    ) {
        if (submissionBox.getTargetScope()
                == SubmissionTargetScope.INDIVIDUAL) {
            return new Owner(
                    submittedBy,
                    null
            );
        }

        Long teamId = studentTeamRepository
                .findActiveTeamIdByStudentId(
                        submittedBy
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode
                                        .SUBMISSION_TEAM_NOT_FOUND
                        )
                );

        return new Owner(
                null,
                teamId
        );
    }

    /**
     * 사용자에게 빠르게 중복 제출 응답을 반환하기 위한
     * 사전 검사입니다.
     *
     * 이 검사가 동시 요청을 완전히 차단하지는 못합니다.
     * 최종 차단은 DB 유니크 제약이 담당합니다.
     */
    private void validateDuplicateSubmission(
            SubmissionBox submissionBox,
            Owner owner
    ) {
        boolean duplicate;

        if (owner.ownerUserId() != null) {
            duplicate = submissionRepository
                    .existsBySubmissionBoxIdAndOwnerUserId(
                            submissionBox.getId(),
                            owner.ownerUserId()
                    );
        } else {
            duplicate = submissionRepository
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

    /**
     * 요청받은 제출 항목을 제출함 항목 ID 기준으로
     * Map으로 변환합니다.
     *
     * 동일한 제출 항목 ID가 중복되면 거절합니다.
     */
    private Map<Long, CreateSubmissionItemCommand>
    createCommandItemMap(
            List<CreateSubmissionItemCommand> items
    ) {
        Map<Long, CreateSubmissionItemCommand> result =
                new HashMap<>();

        for (CreateSubmissionItemCommand item : items) {
            if (item == null
                    || item.submissionBoxItemId()
                    == null
                    || item.submissionBoxItemId() <= 0) {
                throw new BusinessException(
                        ErrorCode
                                .SUBMISSION_ITEM_MISMATCH
                );
            }

            CreateSubmissionItemCommand previous =
                    result.put(
                            item.submissionBoxItemId(),
                            item
                    );

            if (previous != null) {
                throw new BusinessException(
                        ErrorCode
                                .SUBMISSION_ITEM_MISMATCH,
                        "동일한 제출 항목을 중복 입력할 수 없습니다."
                );
            }
        }

        return result;
    }

    /**
     * 요청에 존재하지 않는 제출함 항목이 포함됐는지와
     * 필수 항목이 빠졌는지 검증합니다.
     */
    private void validateRequiredItems(
            SubmissionBox submissionBox,
            Map<Long, CreateSubmissionItemCommand>
                    itemMap
    ) {
        Set<Long> boxItemIds =
                submissionBox.getItems()
                        .stream()
                        .map(
                                SubmissionBoxItem::getId
                        )
                        .collect(
                                Collectors.toSet()
                        );

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
                        .map(
                                SubmissionBoxItem::getId
                        )
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

    /**
     * 제출함 항목 순서대로 실제 제출값을 생성합니다.
     *
     * 사용된 파일 인덱스를 추적해 다음을 차단합니다.
     *
     * 1. 동일한 파일을 여러 항목에서 중복 사용
     * 2. 어떤 항목에도 연결되지 않은 파일 전송
     */
    private List<SubmissionItemValue>
    createItemValues(
            SubmissionBox submissionBox,
            Map<Long, CreateSubmissionItemCommand>
                    commandItemMap,
            List<MultipartFile> files,
            Long submittedBy,
            List<String> uploadedKeys
    ) {
        List<SubmissionItemValue> values =
                new ArrayList<>();

        Set<Integer> usedFileIndexes =
                new HashSet<>();

        for (SubmissionBoxItem boxItem
                : submissionBox.getItems()) {
            CreateSubmissionItemCommand itemCommand =
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
                                submittedBy,
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

        /*
         * 전송된 모든 파일은 정확히 하나의 FILE 항목에서
         * 사용되어야 합니다.
         */
        if (usedFileIndexes.size()
                != files.size()) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ITEM_MISMATCH,
                    "제출 항목에 연결되지 않은 파일이 존재합니다."
            );
        }

        return values;
    }

    /**
     * FILE 제출 항목을 처리합니다.
     */
    private SubmissionItemValue createFileValue(
            SubmissionBox submissionBox,
            SubmissionBoxItem boxItem,
            CreateSubmissionItemCommand command,
            List<MultipartFile> files,
            Long submittedBy,
            List<String> uploadedKeys,
            Set<Integer> usedFileIndexes
    ) {
        if (command.externalUrl() != null
                && !command.externalUrl()
                .isBlank()) {
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

        /*
         * 같은 fileIndex를 두 항목에서 사용할 수 없습니다.
         */
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
                        submittedBy,
                        originalFileName
                );

        /*
         * 도메인 및 DB 길이 제한을 업로드 전에 검사합니다.
         */
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

        /*
         * 업로드가 성공한 키만 등록합니다.
         * 이후 실패하면 등록된 키만 보상 삭제합니다.
         */
        uploadedKeys.add(key);

        return SubmissionItemValue.createFile(
                boxItem.getId(),
                key,
                originalFileName,
                file.getContentType(),
                file.getSize()
        );
    }

    /**
     * LINK 제출 항목을 처리합니다.
     */
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

        validateExternalUrl(
                command.externalUrl()
        );

        return SubmissionItemValue.createLink(
                boxItem.getId(),
                command.externalUrl()
                        .trim()
        );
    }

    /**
     * 제출함에서 설정한 파일 확장자와 실제 파일
     * 확장자가 일치하는지 확인합니다.
     *
     * "*" 설정은 모든 확장자를 허용합니다.
     */
    private void validateAllowedFileType(
            String allowedFileTypes,
            String originalFileName
    ) {
        if (originalFileName == null
                || originalFileName.isBlank()) {
            throw new BusinessException(
                    ErrorCode
                            .SUBMISSION_FILE_TYPE_NOT_ALLOWED
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
                                allowedFileTypes
                                        .split("[,\\s]+")
                        )
                        .map(String::trim)
                        .map(value ->
                                value.startsWith(".")
                                        ? value.substring(1)
                                        : value
                        )
                        .map(value ->
                                value.toLowerCase(
                                        Locale.ROOT
                                )
                        )
                        .filter(value ->
                                !value.isBlank()
                        )
                        .collect(
                                Collectors.toSet()
                        );

        /*
         * "*"는 확장자가 없는 파일을 포함해
         * 모든 파일 형식을 허용합니다.
         */
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
                        .substring(
                                extensionIndex + 1
                        )
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (!allowedExtensions.contains(
                extension
        )) {
            throw new BusinessException(
                    ErrorCode
                            .SUBMISSION_FILE_TYPE_NOT_ALLOWED
            );
        }
    }

    /**
     * 외부 링크가 HTTP 또는 HTTPS URL인지 검증합니다.
     */
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
            URI uri = new URI(trimmedUrl);

            String scheme =
                    uri.getScheme();

            boolean supportedScheme =
                    scheme != null
                            && (scheme.equalsIgnoreCase(
                            "http"
                    )
                            || scheme.equalsIgnoreCase(
                            "https"
                    ));

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

    /**
     * 현재 요청에서 업로드한 S3 파일을 보상 삭제합니다.
     *
     * 삭제 실패는 원래 예외를 가리지 않도록
     * suppressed 예외로 연결합니다.
     */
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

    /**
     * 제출물의 실질적인 소유자입니다.
     *
     * 개인 제출:
     * ownerUserId만 존재
     *
     * 팀 제출:
     * teamId만 존재
     */
    private record Owner(
            Long ownerUserId,
            Long teamId
    ) {
    }
}
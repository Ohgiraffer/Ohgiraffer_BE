package com.ohgiraffer.user.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.s3.S3UrlResolver;
import com.ohgiraffer.user.application.helper.UserFileParserResolver;
import com.ohgiraffer.user.application.policy.UserSheetValidationPolicy;
import com.ohgiraffer.user.application.port.GetTeamNamesByUserIdsPort;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.StudentStatusView;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.model.UserStatus;
import com.ohgiraffer.user.domain.repository.UserRepository;
import com.ohgiraffer.user.presentation.api.response.UserListResponse;
import com.ohgiraffer.user.presentation.api.response.UserResponse;
import com.ohgiraffer.user.presentation.api.response.UserSheetConnectionResponse;
import com.ohgiraffer.user.presentation.api.response.UserSheetRowResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService implements UserQueryUsecase {

    private final UserRepository userRepository;
    private final S3UrlResolver s3UrlResolver;
    private final UserFileParserResolver fileParserResolver;
    private final UserSheetValidationPolicy sheetValidationPolicy;
    private final GetTeamNamesByUserIdsPort getTeamNamesByUserIdsPort;

    @Override
    public UserResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String profileImgUrl = user.getProfileImg() != null
                ? s3UrlResolver.resolve(user.getProfileImg())
                : null;

        return UserResponse.from(user, profileImgUrl);
    }

    @Override
    public UserSheetConnectionResponse checkFileConnection(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_PARSE_FAILED, "업로드된 파일이 비어 있습니다.");
        }

        String filename = file.getOriginalFilename();

        List<List<Object>> allRows = fileParserResolver.resolveAndParse(file);
        if (allRows.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_PARSE_FAILED, "파일에서 읽을 수 있는 데이터가 없습니다.");
        }

        List<UserSheetRowResponse> rows = sheetValidationPolicy.validateRows(allRows);

        return new UserSheetConnectionResponse(filename, rows);
    }

    @Override
    public Long getBootcampId(Long userId) {
        return userRepository.findBootcampIdByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public Role getRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getRole();
    }

    @Override
    public List<Long> getStudentIdsByBootcampId(Long bootcampId) {
        return userRepository.findIdsByBootcampIdAndRole(bootcampId, Role.STUDENT);
    }

    @Override
    public List<StudentStatusView> getStudentStatusesByBootcampId(Long bootcampId) {
        return userRepository.findAllByBootcampIdAndRole(bootcampId, Role.STUDENT).stream()
                .map(u -> new StudentStatusView(u.getId(), u.getStatus()))
                .toList();
    }

    @Override
    public List<UserListResponse> getUsers(Long requesterId) {
        Long bootcampId = userRepository.findBootcampIdByUserId(requesterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<User> users = userRepository.findAllByBootcampId(bootcampId);

        List<Long> userIds = users.stream()
                .map(User::getId)
                .toList();
        Map<Long, String> teamNames = getTeamNamesByUserIdsPort.findTeamNamesByUserIds(userIds);

        return users.stream()
                .map(user -> {
                    String profileImgUrl = user.getProfileImg() != null
                            ? s3UrlResolver.resolve(user.getProfileImg())
                            : null;
                    return UserListResponse.of(user, teamNames.get(user.getId()), profileImgUrl);
                })
                .toList();
    }

    @Override
    public Map<String, Long> getStudentEmailToIdMapByBootcampId(Long bootcampId) {
        List<User> students = userRepository.findAllByBootcampIdAndRole(bootcampId, Role.STUDENT);

        return students.stream()
                .collect(Collectors.toMap(User::getEmail, User::getId, (a, b) -> a));
    }

    @Override
    public Long getAnyActiveBootcampId() {
        return userRepository.findAnyBootcampIdByStatus(UserStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOTCAMP_NOT_FOUND));
    }
}
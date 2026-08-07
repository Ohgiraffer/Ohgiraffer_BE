package com.ohgiraffer.user.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.google.sheets.GoogleSheetsClient;
import com.ohgiraffer.global.google.sheets.SpreadsheetIdExtractor;
import com.ohgiraffer.global.s3.S3UrlResolver;
import com.ohgiraffer.user.application.policy.UserSheetValidationPolicy;
import com.ohgiraffer.user.application.usecase.UserQueryUsecase;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import com.ohgiraffer.user.presentation.api.response.UserResponse;
import com.ohgiraffer.user.presentation.api.response.UserSheetConnectionResponse;
import com.ohgiraffer.user.presentation.api.response.UserSheetRowResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService implements UserQueryUsecase {

    private final UserRepository userRepository;
    private final S3UrlResolver s3UrlResolver;
    private final SpreadsheetIdExtractor idExtractor;
    private final GoogleSheetsClient sheetsClient;
    private final UserSheetValidationPolicy sheetValidationPolicy;

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
    public UserSheetConnectionResponse checkSheetConnection(String spreadsheetUrl) {
        String spreadsheetId = idExtractor.extract(spreadsheetUrl);
        Optional<Long> requestedGid = idExtractor.extractGid(spreadsheetUrl);

        String title = sheetsClient.getSpreadsheetTitle(spreadsheetId);

        List<GoogleSheetsClient.SheetInfo> sheetInfos = sheetsClient.getSheetInfos(spreadsheetId);
        if (sheetInfos.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_SHEET_API_ERROR,
                    "조회 가능한 시트 탭이 없습니다."
            );
        }

        // gid가 URL에 있으면 그 탭을 찾고 없으면 첫 번째 탭 사용
        GoogleSheetsClient.SheetInfo targetSheet = requestedGid
                .map(gid -> sheetInfos.stream()
                        .filter(info -> info.gid() == gid)
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(
                                ErrorCode.GOOGLE_SHEET_INVALID_URL,
                                "URL에 해당하는 시트 탭을 찾을 수 없습니다."
                        )))
                .orElse(sheetInfos.get(0));

        List<String> sheetNames = sheetInfos.stream()
                .map(GoogleSheetsClient.SheetInfo::name)
                .toList();

        List<List<Object>> allRows = sheetsClient.readRange(
                spreadsheetId, buildFullRange(targetSheet.name())
        );

        List<String> columns = allRows.isEmpty() ? List.of() : extractColumns(allRows.get(0));
        List<UserSheetRowResponse> rows = sheetValidationPolicy.validateRows(allRows);

        long validCount = rows.stream().filter(UserSheetRowResponse::valid).count();

        return new UserSheetConnectionResponse(
                title,
                sheetNames,
                targetSheet.name(),
                columns,
                rows.size(),
                (int) validCount,
                rows.size() - (int) validCount,
                rows
        );
    }

    @Override
    public Long getBootcampId(Long userId) {
        return userRepository.findBootcampIdByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private String buildFullRange(String sheetName) {
        String escaped = sheetName.replace("'", "''");
        return "'" + escaped + "'!A1:D1000";
    }

    private List<String> extractColumns(List<Object> headerRow) {
        return headerRow.stream()
                .map(cell -> cell == null ? "" : cell.toString().trim())
                .toList();
    }
}
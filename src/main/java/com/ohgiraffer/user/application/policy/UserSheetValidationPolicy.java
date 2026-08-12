package com.ohgiraffer.user.application.policy;

import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.repository.UserRepository;
import com.ohgiraffer.user.presentation.api.response.UserSheetRowResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class UserSheetValidationPolicy {

    private final UserRepository userRepository;

    public List<UserSheetRowResponse> validateRows(List<List<Object>> allRows) {
        if (allRows.size() <= 1) {
            return List.of();
        }

        List<UserSheetRowResponse> parsed = IntStream.range(1, allRows.size())
                .mapToObj(i -> validateRow(i + 1, allRows.get(i)))
                .toList();

        return markDuplicateEmails(parsed);
    }

    private UserSheetRowResponse validateRow(int rowNumber, List<Object> row) {
        List<String> errors = new ArrayList<>();

        String name = getCell(row, 0);
        String email = getCell(row, 1);
        String phone = getCell(row, 2);
        String rawRole = getCell(row, 3);

        if (isBlank(name)) {
            errors.add("이름 없음");
        }

        if (isBlank(email)) {
            errors.add("이메일 없음");
        } else if (!isValidEmail(email)) {
            errors.add("이메일 형식 오류");
        } else if (userRepository.existsByEmail(email)) {
            errors.add("이미 등록된 이메일");
        }

        if (isBlank(phone) || !isValidPhone(phone)) {
            errors.add("전화번호 형식 오류");
        }

        Role parsedRole = Role.fromSheetDisplayName(rawRole);
        if (parsedRole == null) {
            errors.add(isBlank(rawRole)
                    ? "역할 없음"
                    : "역할 값을 인식할 수 없습니다: " + rawRole);
        }

        return new UserSheetRowResponse(
                rowNumber, name, email, phone, rawRole, errors.isEmpty(), errors
        );
    }

    private List<UserSheetRowResponse> markDuplicateEmails(List<UserSheetRowResponse> rows) {
        Map<String, Long> emailCounts = rows.stream()
                .filter(r -> !isBlank(r.email()) && isValidEmail(r.email()))
                .collect(Collectors.groupingBy(UserSheetRowResponse::email, Collectors.counting()));

        return rows.stream()
                .map(r -> {
                    if (emailCounts.getOrDefault(r.email(), 0L) > 1) {
                        List<String> merged = new ArrayList<>(r.errors());
                        merged.add("시트 내 중복된 이메일");
                        return new UserSheetRowResponse(
                                r.rowNumber(), r.name(), r.email(), r.phone(),
                                r.rawRole(), false, merged
                        );
                    }
                    return r;
                })
                .toList();
    }

    private String getCell(List<Object> row, int index) {
        if (index >= row.size()) return "";
        Object value = row.get(index);
        return value == null ? "" : value.toString().trim();
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w.+-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z]{2,}$");
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^01[0-9]-?\\d{3,4}-?\\d{4}$");
    }
}
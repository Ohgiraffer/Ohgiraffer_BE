package com.ohgiraffer.attendance.application.helper;

import com.ohgiraffer.attendance.domain.model.AttendanceStatus;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AttendanceStatusResolver {

    private static final Map<String, AttendanceStatus> LABEL_TO_STATUS = Map.of(
            "결석", AttendanceStatus.ABSENT,
            "외출", AttendanceStatus.OUTING,
            "조퇴", AttendanceStatus.EARLY_LEAVE,
            "지각", AttendanceStatus.LATE,
            "출석", AttendanceStatus.PRESENT,
            "병결", AttendanceStatus.SICK,
            "휴가", AttendanceStatus.LEAVE
    );

    public AttendanceStatus resolve(String rawLabel) {
        if (rawLabel == null || rawLabel.isBlank()) {
            return null;
        }
        AttendanceStatus status = LABEL_TO_STATUS.get(rawLabel.trim());
        if (status == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "알 수 없는 출결 상태값입니다: " + rawLabel);
        }
        return status;
    }
}
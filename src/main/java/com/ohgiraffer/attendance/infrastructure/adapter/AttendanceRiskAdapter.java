package com.ohgiraffer.attendance.infrastructure.adapter;

import com.ohgiraffer.aiassistant.application.port.AttendanceRiskPort;
import com.ohgiraffer.aiassistant.domain.model.AttendanceRiskInfo;
import com.ohgiraffer.attendance.domain.dto.AttendanceSummaryView;
import com.ohgiraffer.attendance.domain.repository.AttendanceRepository;
import com.ohgiraffer.bootcamp.application.port.GetUserBootcampIdPort;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.model.UserStatus;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/*
 * comment.
 *  AttendanceRiskPort 실구현체 - AI비서 전용 (TODO에서 이전됨, TODO 카드엔 노출 안 함)
 *  - STUDENT: 본인 위험도만 단건 조회
 *  - INSTRUCTOR/MANAGER: 같은 부트캠프 소속 재원 학생 전체 순회하며 위험군만 필터링
 *  - 위험도 기준은 여전히 임시값 (실제 정책 확정 전까지 lateCount+earlyLeaveCount+absentDays 합산 기준)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceRiskAdapter implements AttendanceRiskPort {

    private final AttendanceRepository attendanceRepository;  // 출결 도메인 Repository 직접 주입
    private final UserRepository userRepository;              // 담당 훈련생 목록 조회용
    private final GetUserBootcampIdPort getUserBootcampIdPort;    // 요청자(강사/매니저)의 bootcampId 조회용

    private static final int WARNING_THRESHOLD = 3;   // 임시 기준: 지각+조퇴+결석 누적 3회부터 "주의"
    private static final int DANGER_THRESHOLD = 6;     // 임시 기준: 6회부터 "경고"

    // role별 출결 위험도 요약
    @Override
    public List<AttendanceRiskInfo> getRiskItems(Long userId, Role role) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        if (role == Role.STUDENT) {
            AttendanceRiskInfo own = toRiskItemOrNull(userId, monthStart, today);
            return own == null ? List.of() : List.of(own);
        }

        Long bootcampId = getUserBootcampIdPort.findBootcampIdByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOTCAMP_ACCESS_DENIED));

        List<User> scopedStudents = userRepository.findAllByRoleAndStatusAndBootcampId(
                Role.STUDENT, UserStatus.ACTIVE, bootcampId
        );

        return scopedStudents.stream()
                .map(student -> toRiskItemOrNull(student.getId(), monthStart, today))
                .filter(item -> item != null)
                .toList();
    }

    private AttendanceRiskInfo toRiskItemOrNull(Long targetUserId, LocalDate start, LocalDate end) {
        AttendanceSummaryView summary = attendanceRepository.countByUserAndDateRange(targetUserId, start, end);

        long riskCount = summary.lateCount() + summary.earlyLeaveCount() + summary.absentDays();
        String riskLevel = resolveRiskLevel(riskCount);

        if ("정상".equals(riskLevel)) {
            return null;
        }

        return new AttendanceRiskInfo(targetUserId, riskLevel, LocalDateTime.now());
    }

    private String resolveRiskLevel(long riskCount) {
        if (riskCount >= DANGER_THRESHOLD) {
            return "경고";
        }
        if (riskCount >= WARNING_THRESHOLD) {
            return "주의";
        }
        return "정상";
    }

}
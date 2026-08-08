package com.ohgiraffer.todo.infrastructure.adapter;

import com.ohgiraffer.attendance.domain.model.AttendanceSummaryView;
import com.ohgiraffer.attendance.domain.repository.AttendanceRepository;
import com.ohgiraffer.bootcamp.application.port.GetUserBootcampIdPort;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.todo.application.port.AttendanceTodoPort;
import com.ohgiraffer.todo.domain.model.TodoItemResponse;
import com.ohgiraffer.todo.domain.model.TodoSourceDomain;
import com.ohgiraffer.todo.domain.model.TodoResponse;
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

/* comment.
 *  AttendanceTodoPort 임시구현체
 *  - 전체 학생 배치 조회 API(/attendance/monthly 등 관리자용)가 아직 미개발 상태라,
 *    UserRepository.findAllByRoleAndStatus(STUDENT, ACTIVE)로 재원생 목록을 가져온 뒤
 *    학생 1명씩 AttendanceRepository.countByUserAndDateRange를 호출하는 방식으로 우회 (N+1이지만 학생 수 규모상 허용)
 *  - 위험도 기준은 여전히 임시값 (실제 정책 확정 전까지 lateCount+earlyLeaveCount+absentDays 합산 기준)
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceTodoAdapter implements AttendanceTodoPort {

    private final AttendanceRepository attendanceRepository;  // 출결 도메인 Repository 직접 주입
    private final UserRepository userRepository;              // 담당 훈련생 목록 조회용
    private final GetUserBootcampIdPort getUserBootcampIdPort;    // 요청자(강사/매니저)의 bootcampId 조회용

    private static final int WARNING_THRESHOLD = 3;   // 임시 기준: 지각+조퇴+결석 누적 3회부터 "주의"
    private static final int DANGER_THRESHOLD = 6;     // 임시 기준: 6회부터 "경고"

    // role별 출결 위험도 요약
    @Override
    public TodoResponse getSummary(Long userId, Role role) {
        List<TodoItemResponse> pendingItems = getPendingItems(userId, role);  // 상세 리스트 재활용해서 건수 산출
        return new TodoResponse(TodoSourceDomain.ATTENDANCE, "출결 위험", pendingItems.size(), null);
    }

    // 출결 위험 상태 상세 리스트 - 강사/매니저 전용, 재원 훈련생 전체 순회하며 위험군만 필터링
    @Override
    public List<TodoItemResponse> getPendingItems(Long userId, Role role) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        Long bootcampId = getUserBootcampIdPort.findBootcampIdByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOTCAMP_ACCESS_DENIED));  // 소속 부트캠프 없으면 조회 자체를 막음

        List<User> scopedStudents = userRepository.findAllByRoleAndStatusAndBootcampId(
                Role.STUDENT, UserStatus.ACTIVE, bootcampId
        );

        return scopedStudents.stream()
                .map(student -> toRiskItem(student, monthStart, today))
                .filter(item -> item != null)  // 정상인 학생은 TODO 노출 대상 아님
                .toList();
    }

    // 학생 1명의 이번 달 출결 집계 -> 위험도 판정 -> TODO 항목 변환 (정상이면 null)
    private TodoItemResponse toRiskItem(User student, LocalDate start, LocalDate end) {
        AttendanceSummaryView summary = attendanceRepository.countByUserAndDateRange(student.getId(), start, end);

        long riskCount = summary.lateCount() + summary.earlyLeaveCount() + summary.absentDays();
        String riskLevel = resolveRiskLevel(riskCount);

        if ("정상".equals(riskLevel)) {
            return null;
        }

        return new TodoItemResponse(
                TodoSourceDomain.ATTENDANCE,
                student.getId(),
                riskLevel,
                riskLevel,
                LocalDateTime.now(),
                null
        );
    }

    // 임시 위험도 산정 로직 - 실제 기준 확정되면 교체
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
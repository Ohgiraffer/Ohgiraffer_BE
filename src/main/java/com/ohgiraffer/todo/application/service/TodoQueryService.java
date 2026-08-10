package com.ohgiraffer.todo.application.service;

import com.ohgiraffer.todo.application.port.*;
import com.ohgiraffer.todo.application.usecase.TodoQueryUseCase;
import com.ohgiraffer.todo.domain.model.TodoResponse;
import com.ohgiraffer.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/* comment.
 *  TODO 도메인 취합 Service, 6개 Port를 role 기준으로 조건 분기해서 통합 응답 조립
 *  - role별 노출 Port 구성:
 *    훈련생 = Submission, Approval, Notice, Evaluation, Consultation
 *    강사   = Approval, Notice, Evaluation, Consultation, Attendance
 *    매니저 = Approval, Notice, Consultation, Attendance
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class TodoQueryService implements TodoQueryUseCase {

    private final SubmissionTodoPort submissionTodoPort;      // 제출물 Port
    private final ApprovalTodoPort approvalTodoPort;          // 결재 Port
    private final NoticeTodoPort noticeTodoPort;               // 공지 Port
    private final EvaluationTodoPort evaluationTodoPort;       // 평가 Port (Mock 경유)
    private final ConsultationTodoPort consultationTodoPort;   // 상담 Port (Mock 경유)
    private final AttendanceTodoPort attendanceTodoPort;       // 출결 Port

    // role별로 노출할 Port만 골라 getSummary 호출해서 요약 리스트 조립
    @Override
    public List<TodoResponse> getSummaries(Long userId, Role role) {
        List<TodoResponse> summaries = new ArrayList<>();

        if (isSubmissionVisible(role)) {
            summaries.add(submissionTodoPort.getSummary(userId, role));
        }
        if (isApprovalVisible(role)) {
            summaries.add(approvalTodoPort.getSummary(userId, role));
        }
        if (isNoticeVisible(role)) {
            summaries.add(noticeTodoPort.getSummary(userId, role));
        }
        if (isEvaluationVisible(role)) {
            summaries.add(evaluationTodoPort.getSummary(userId, role));
        }
        if (isConsultationVisible(role)) {
            summaries.add(consultationTodoPort.getSummary(userId, role));
        }
        if (isAttendanceVisible(role)) {
            summaries.add(attendanceTodoPort.getSummary(userId, role));
        }

        return summaries;
    }

    // 제출물: 훈련생만 노출
    private boolean isSubmissionVisible(Role role) {
        return role == Role.STUDENT;
    }

    // 결재: 전 role 공통 노출 (훈련생=본인신청, 강사/매니저=처리대상 - Port 내부에서 필터링)
    private boolean isApprovalVisible(Role role) {
        return true;
    }

    // 공지: 전 role 공통 노출
    private boolean isNoticeVisible(Role role) {
        return true;
    }

    // 평가: 훈련생/강사만 노출 (매니저는 제외)
    private boolean isEvaluationVisible(Role role) {
        return role == Role.STUDENT || role == Role.INSTRUCTOR;
    }

    // 상담: 전 role 공통 노출
    private boolean isConsultationVisible(Role role) {
        return true;
    }

    // 출결: 강사/매니저만 노출 (훈련생 본인 출결은 대시보드 출결현황 카드에서 이미 확인 가능하므로 TODO엔 미노출)
    private boolean isAttendanceVisible(Role role) {
        return role == Role.INSTRUCTOR || role == Role.MANAGER;
    }

}

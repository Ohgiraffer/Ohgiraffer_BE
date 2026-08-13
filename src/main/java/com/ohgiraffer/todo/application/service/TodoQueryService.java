package com.ohgiraffer.todo.application.service;

import com.ohgiraffer.todo.application.port.*;
import com.ohgiraffer.todo.application.usecase.TodoQueryUseCase;
import com.ohgiraffer.todo.domain.model.TodoItemResponse;
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
    private final ConsultationTodoPort consultationTodoPort;   // 상담 Port (Mock 경유)

    // role별로 노출할 Port만 골라 getSummary 호출해서 요약 리스트 조립
    @Override
    public List<TodoResponse> getSummaries(Long userId, Role role) {
        List<TodoResponse> summaries = new ArrayList<>();
        if (isSubmissionVisible(role)) {
            summaries.add(submissionTodoPort.getSummary(userId, role));
        }
        summaries.add(approvalTodoPort.getSummary(userId, role));
        summaries.add(noticeTodoPort.getSummary(userId, role));
        summaries.add(consultationTodoPort.getSummary(userId, role));
        return summaries;
    }

    private boolean isSubmissionVisible(Role role) {
        return role == Role.STUDENT;
    }

}

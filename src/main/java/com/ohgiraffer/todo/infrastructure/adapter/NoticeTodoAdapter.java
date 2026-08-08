package com.ohgiraffer.todo.infrastructure.adapter;

import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.model.ViewerRole;
import com.ohgiraffer.notice.domain.repository.NoticeConfirmationRepository;
import com.ohgiraffer.notice.domain.repository.NoticeRepository;
import com.ohgiraffer.todo.application.port.NoticeTodoPort;
import com.ohgiraffer.todo.domain.model.TodoItemResponse;
import com.ohgiraffer.todo.domain.model.TodoSourceDomain;
import com.ohgiraffer.todo.domain.model.TodoResponse;
import com.ohgiraffer.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

/* comment.
 *  NoticeTodoPort 실구현체
 *  - role -> ViewerRole 매핑: STUDENT=TRAINEE, INSTRUCTOR/MANAGER=STAFF
 *  - findAllVisible로 조회 가능한 공지 전체를 가져온 뒤, NoticeConfirmationRepository로 미확인 건만 필터링
 *  - Notice.createdAt이 Instant라 KST(Asia/Seoul) 기준 LocalDateTime으로 변환
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeTodoAdapter implements NoticeTodoPort {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");  // Instant -> LocalDateTime 변환 기준

    private final NoticeRepository noticeRepository;                        // 공지 도메인 Repository 직접 주입
    private final NoticeConfirmationRepository noticeConfirmationRepository; // 확인 기록 조회용

    // 미확인 공지 건수 요약
    @Override
    public TodoResponse getSummary(Long userId, Role role) {
        List<TodoItemResponse> pendingItems = getPendingItems(userId, role);  // 상세 리스트 재활용해서 건수 산출
        return new TodoResponse(TodoSourceDomain.NOTICE, "미확인 공지", pendingItems.size(), null);
    }

    // 미확인 공지 상세 리스트
    @Override
    public List<TodoItemResponse> getPendingItems(Long userId, Role role) {
        ViewerRole viewerRole = role == Role.STUDENT ? ViewerRole.TRAINEE : ViewerRole.STAFF;

        List<Notice> visibleNotices = noticeRepository.findAllVisible(viewerRole, null);  // 카테고리 필터 없이 전체 조회
        List<Long> noticeIds = visibleNotices.stream().map(Notice::getId).toList();

        Set<Long> confirmedIds = noticeConfirmationRepository.findConfirmedNoticeIds(userId, noticeIds);  // 이미 확인한 공지 id만 별도 조회

        return visibleNotices.stream()
                .filter(notice -> !confirmedIds.contains(notice.getId()))  // 미확인 공지만 필터링
                .map(this::toTodoItemResponse)
                .toList();
    }

    // Notice 도메인 모델 -> TodoItemResponse 변환
    private TodoItemResponse toTodoItemResponse(Notice notice) {
        return new TodoItemResponse(
                TodoSourceDomain.NOTICE,
                notice.getId(),
                "공지",
                notice.isPinned() ? "고정" : "일반",
                LocalDateTime.ofInstant(notice.getCreatedAt(), KST),
                null  // 공지는 마감 개념 없음
        );
    }

}
package com.ohgiraffer.notification.presentation.api.controller;

import com.ohgiraffer.notification.application.command.NotificationBulkDeleteCommand;
import com.ohgiraffer.notification.application.command.NotificationCreateCommand;
import com.ohgiraffer.notification.application.result.NotificationResult;
import com.ohgiraffer.notification.application.usecase.NotificationCommandUseCase;
import com.ohgiraffer.notification.application.usecase.NotificationQueryUseCase;
import com.ohgiraffer.notification.domain.model.NotificationType;
import com.ohgiraffer.notification.presentation.api.request.NotificationBulkDeleteRequest;
import com.ohgiraffer.notification.presentation.api.request.NotificationCreateRequest;
import com.ohgiraffer.notification.presentation.api.response.BulkDeleteResponse;
import com.ohgiraffer.notification.presentation.api.response.NotificationResponse;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationQueryUseCase notificationQueryUseCase;
    private final NotificationCommandUseCase notificationCommandUseCase;

    // 알림 목록 조회 - isRead/type 없으면 전체, 페이징 없음
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) NotificationType type
    ) {
        List<NotificationResponse> result = notificationQueryUseCase
                .getNotifications(principal.getId(), isRead, type)
                .stream()
                .map(NotificationResponse::from)
                .toList();

        return ResponseEntity.ok(result);
    }

    // 알림 읽음 처리
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long notificationId
    ) {
        NotificationResult result = notificationCommandUseCase.markAsRead(notificationId, principal.getId());
        return ResponseEntity.ok(NotificationResponse.from(result));
    }

    // 알림 개별 삭제 - 하드 딜리트
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long notificationId
    ) {
        notificationCommandUseCase.delete(notificationId, principal.getId());
        return ResponseEntity.noContent().build();
    }

    // 알림 선택(전체 포함) 삭제 - 프론트가 선택된 id 배열 전부 담아서 호출, 본인 소유 아닌 id는 조용히 스킵
    @DeleteMapping
    public ResponseEntity<BulkDeleteResponse> bulkDeleteNotifications(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody NotificationBulkDeleteRequest request
    ) {
        long deletedCount = notificationCommandUseCase.bulkDelete(
                new NotificationBulkDeleteCommand(request.notificationIds(), principal.getId())
        );
        return ResponseEntity.ok(new BulkDeleteResponse(deletedCount));
    }

    // 알림 생성(내부용) - 외부 미노출, 각 도메인(전자결재/공지/캘린더/출결/상담/제출/채팅)에서 서비스 간 호출
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationCreateRequest request
    ) {
        NotificationResult result = notificationCommandUseCase.create(
                new NotificationCreateCommand(
                        request.userId(),
                        request.notificationType(),
                        request.title(),
                        request.content(),
                        request.relatedEntityType(),
                        request.relatedEntityId()
                )
        );
        return ResponseEntity.ok(NotificationResponse.from(result));
    }


}

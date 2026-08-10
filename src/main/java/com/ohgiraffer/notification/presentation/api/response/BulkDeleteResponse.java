package com.ohgiraffer.notification.presentation.api.response;

// 선택 삭제응답 - 실제 삭제된 건수만 반환
public record BulkDeleteResponse(
        long deletedCount
) {
}

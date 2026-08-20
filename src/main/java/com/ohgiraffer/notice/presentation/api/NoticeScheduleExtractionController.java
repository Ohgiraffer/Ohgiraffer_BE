package com.ohgiraffer.notice.presentation.api;

import com.ohgiraffer.global.exception.ErrorResponse;
import com.ohgiraffer.notice.application.usecase.NoticeScheduleExtractionUseCase;
import com.ohgiraffer.notice.presentation.api.response.ExtractedScheduleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "공지 AI 일정 추출", description = "공지 상세의 AI 일정 등록 컴포넌트")
@RestController
@RequestMapping("/notices/{noticeId}/schedule-extraction")
public class NoticeScheduleExtractionController {

    private final NoticeScheduleExtractionUseCase noticeScheduleExtractionUseCase;

    public NoticeScheduleExtractionController(
            NoticeScheduleExtractionUseCase noticeScheduleExtractionUseCase
    ) {
        this.noticeScheduleExtractionUseCase = noticeScheduleExtractionUseCase;
    }

    @Operation(
            summary = "공지에서 일정 후보 추출",
            description = """
                    공지 상세의 [등록하기] 버튼이 호출한다. 본문을 AI 에 보내
                    캘린더에 넣을 만한 일정을 찾아 돌려준다.

                    저장하지 않는다. 사용자가 모달에서 값을 고치거나 후보를 뺄 수 있어,
                    여기서 저장해 두면 버려질 값을 남기게 된다. 확정한 일정은
                    POST /notices/{noticeId}/calendar-events 로 따로 등록한다.

                    빈 배열이 올 수 있다. 일정이 없는 공지가 더 흔하다.
                    화면은 '공지사항에서 추출된 일정이 없습니다.' 로 안내하면 된다.

                    eventType 은 null 일 수 있다. 본문만 보고 수업인지 행사인지 가릴 수 없으면
                    채우지 않는다. 아무 값이나 채우면 사용자가 그대로 등록해 틀린 유형이 남는다.
                    화면에서 사용자가 고르게 하고, 고르지 않은 후보는 등록에서 빼면 된다.

                    시각도 null 일 수 있다. "오전 중" 처럼 흐릿하게 적힌 일정이 흔하다.
                    그때는 종일 일정으로 다루면 된다.

                    AI 를 호출하므로 수 초 걸린다. 누른 뒤 버튼을 잠가야 같은 공지로
                    여러 번 호출되지 않는다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추출 성공. 비어 있을 수 있다"),
            @ApiResponse(
                    responseCode = "403",
                    description = "훈련생이 호출 (AUTH_002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 공지 (NOTICE_001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "AI 호출 실패 또는 응답을 읽지 못함 (NOTICE_012, AI_001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @PostMapping
    public ResponseEntity<List<ExtractedScheduleResponse>> extract(
            @Parameter(description = "공지 식별자", example = "1")
            @PathVariable Long noticeId
    ) {
        return ResponseEntity.ok(
                noticeScheduleExtractionUseCase.extract(noticeId)
                        .stream()
                        .map(ExtractedScheduleResponse::from)
                        .toList()
        );
    }
}

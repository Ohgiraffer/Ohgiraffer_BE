package com.ohgiraffer.notice.presentation.api;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.exception.ErrorResponse;
import com.ohgiraffer.global.s3.S3UrlResolver;
import com.ohgiraffer.notice.application.usecase.NoticeAttachmentCommandUseCase;
import com.ohgiraffer.notice.domain.model.NoticeAttachment;
import com.ohgiraffer.notice.presentation.api.response.NoticeAttachmentResponse;
import com.ohgiraffer.notice.presentation.api.response.UploadedNoticeAttachmentResponse;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "공지 첨부파일", description = "공지 상세 화면의 첨부파일")
@RestController
@RequestMapping("/notices")
public class NoticeAttachmentController {

    private final NoticeAttachmentCommandUseCase noticeAttachmentCommandUseCase;
    private final S3UrlResolver s3UrlResolver;

    public NoticeAttachmentController(
            NoticeAttachmentCommandUseCase noticeAttachmentCommandUseCase,
            S3UrlResolver s3UrlResolver
    ) {
        this.noticeAttachmentCommandUseCase = noticeAttachmentCommandUseCase;
        this.s3UrlResolver = s3UrlResolver;
    }

    @Operation(
            summary = "공지 등록 전 첨부파일 올리기",
            description = """
                    공지 작성 화면에서 파일을 고르는 순간 호출한다. 공지 번호가 필요 없다.

                    돌려주는 fileKey 를 공지 등록 요청의 attachments 에 그대로 실어 보내면
                    그때 공지와 이어진다. DB 에는 아직 아무것도 남지 않는다.

                    화면의 등록 버튼이 하나이므로 이렇게 나눈다. 공지를 먼저 저장하고
                    첨부를 따로 올리면 그 사이에 실패했을 때 첨부 없는 공지가 남고,
                    사용자는 실패한 줄 알고 다시 눌러 공지를 두 번 만들게 된다.

                    한 요청에 여러 개를 보낼 수 있고, 같은 이름(files)으로 파트를 반복하면 된다.
                    5개까지, 파일 하나는 10MB 까지, PDF / DOCX / XLS / HWP / JPG / PNG 다.
                    한 건이라도 조건에 걸리면 아무것도 올라가지 않는다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "업로드 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            파일 없음, 빈 파일 (COMMON_001), 개수 초과 (NOTICE_008),
                            크기 초과 (NOTICE_009), 허용하지 않는 형식 (NOTICE_011)
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasAnyRole('MANAGER', 'INSTRUCTOR')")
    @PostMapping(
            value = "/attachments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<List<UploadedNoticeAttachmentResponse>> uploadBeforeNotice(
            @RequestPart("files") List<MultipartFile> files
    ) {
        return ResponseEntity
                .status(201)
                .body(noticeAttachmentCommandUseCase
                        .uploadBeforeNotice(files)
                        .stream()
                        .map(UploadedNoticeAttachmentResponse::from)
                        .toList());
    }

    @Operation(
            summary = "이미 등록된 공지에 첨부파일 추가",
            description = """
                    공지 작성자만 올릴 수 있다. 수정 화면에서 첨부를 나중에 더할 때 쓴다.

                    새 공지를 등록하는 흐름에서는 이 API 가 아니라
                    POST /notices/attachments 로 먼저 올린 뒤 공지 등록에 실어 보낸다.

                    공지 하나에 5개까지이며 이미 올라간 개수를 합쳐서 센다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "업로드 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            파일 없음, 빈 파일 (COMMON_001),
                            개수 초과 (NOTICE_008), 크기 초과 (NOTICE_009)
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "작성자가 아님 (NOTICE_003)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 공지 (NOTICE_001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping(
            value = "/{noticeId}/attachments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<List<NoticeAttachmentResponse>> upload(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "공지 식별자", example = "1")
            @PathVariable Long noticeId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        List<NoticeAttachment> uploaded = noticeAttachmentCommandUseCase.upload(
                noticeId,
                currentUserId(principal),
                files
        );

        return ResponseEntity
                .status(201)
                .body(uploaded.stream().map(this::toResponse).toList());
    }

    @Operation(
            summary = "공지 첨부파일 삭제",
            description = """
                    공지 작성자만 지울 수 있다. DB 행과 저장소의 파일을 함께 지운다.

                    경로의 공지에 속하지 않은 첨부 번호를 보내면 404 로 답한다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(
                    responseCode = "403",
                    description = "작성자가 아님 (NOTICE_003)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = """
                            존재하지 않는 공지 (NOTICE_001)
                            또는 그 공지의 첨부가 아님 (NOTICE_007)
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/{noticeId}/attachments/{noticeAttachmentId}")
    public ResponseEntity<Void> delete(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "공지 식별자", example = "1")
            @PathVariable Long noticeId,
            @Parameter(description = "첨부파일 식별자", example = "1")
            @PathVariable Long noticeAttachmentId
    ) {
        noticeAttachmentCommandUseCase.delete(
                noticeId,
                noticeAttachmentId,
                currentUserId(principal)
        );

        return ResponseEntity.noContent().build();
    }

    private NoticeAttachmentResponse toResponse(NoticeAttachment attachment) {
        return NoticeAttachmentResponse.from(
                attachment,
                s3UrlResolver.resolveDownload(
                        attachment.getFileKey(),
                        attachment.getFileName()
                )
        );
    }

    /**
     * SecurityConfig 가 인증을 요구하므로 정상 흐름에서는 null 이 아니다.
     * 설정이 바뀌어 인증 없이 도달했을 때 NullPointerException 대신 401 로 알리기 위한 방어다.
     */
    private Long currentUserId(CustomUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return principal.getId();
    }
}

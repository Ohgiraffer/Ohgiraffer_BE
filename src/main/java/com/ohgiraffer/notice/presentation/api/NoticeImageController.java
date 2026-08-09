package com.ohgiraffer.notice.presentation.api;

import com.ohgiraffer.global.exception.ErrorResponse;
import com.ohgiraffer.notice.application.usecase.NoticeImageCommandUseCase;
import com.ohgiraffer.notice.presentation.api.response.NoticeImageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@Tag(name = "공지 본문 이미지", description = "공지 작성 화면의 에디터에서 사용")
@RestController
@RequestMapping("/notices/images")
public class NoticeImageController {

    private final NoticeImageCommandUseCase noticeImageCommandUseCase;

    public NoticeImageController(
            NoticeImageCommandUseCase noticeImageCommandUseCase
    ) {
        this.noticeImageCommandUseCase = noticeImageCommandUseCase;
    }

    @Operation(
            summary = "공지 본문 이미지 업로드",
            description = """
                    에디터에서 이미지를 넣을 때 호출한다. 돌려주는 주소를 본문에 그대로 쓰면 된다.

                    공지 번호를 받지 않는다. 글을 쓰는 중이라 아직 공지가 저장되기 전이기 때문이다.
                    이미지를 먼저 올려 주소를 받고, 그 주소가 담긴 본문을 공지 등록에 실어 보내면 된다.

                    첨부파일 API 와 다른 API 다. 첨부파일은 공지 하단에 목록으로 붙고,
                    이 API 는 본문 안에 그려진다.

                    돌려주는 것은 S3 주소가 아니라 이 서버의 상대 경로다.
                    (예: /notices/images/uuid.png)
                    화면에서 API 기본 주소를 앞에 붙여 본문에 넣으면 된다.
                    환경마다 서버 주소가 달라 상대 경로로 준다.

                    S3 주소를 직접 주지 않는 이유는, 만료되는 주소는 본문에 넣을 수 없고
                    만료되지 않게 하려면 저장소를 공개해야 하기 때문이다.
                    이 경로로 요청이 오면 서버가 그때 주소를 만들어 넘겨준다.

                    JPG / PNG, 한 장에 5MB 까지다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "업로드 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            이미지 없음 (COMMON_001), 크기 초과 (NOTICE_009),
                            허용하지 않는 형식 (NOTICE_010)
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "운영진이 아님",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NoticeImageResponse> upload(
            @RequestPart("image") MultipartFile image
    ) {
        return ResponseEntity
                .status(201)
                .body(new NoticeImageResponse(
                        noticeImageCommandUseCase.upload(image)
                ));
    }

    @Operation(
            summary = "공지 본문 이미지 조회",
            description = """
                    본문의 img 태그가 부르는 경로다. 저장소의 실제 주소로 넘겨준다(302).

                    인증을 요구하지 않는다. 브라우저의 img 태그는 Authorization 헤더를
                    붙이지 않아, 인증을 걸면 본문 이미지가 전부 깨지기 때문이다.
                    대신 파일명이 UUID 라 주소를 모르면 찾아낼 수 없다.

                    저장소를 공개하지 않고 이 경로를 두는 이유가 여기 있다.
                    공개 저장소는 한 번 열면 그 아래 전부가 열리지만,
                    이 경로는 서버를 거치므로 나중에 조건을 붙일 수 있다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "저장소 주소로 이동"),
            @ApiResponse(
                    responseCode = "400",
                    description = "파일명 형식이 올바르지 않음 (COMMON_001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{fileName}")
    public ResponseEntity<Void> serve(
            @Parameter(description = "업로드 응답에 담겨 온 파일명", example = "uuid.png")
            @PathVariable String fileName
    ) {
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(
                        noticeImageCommandUseCase.resolveUrl(fileName)))
                .build();
    }
}

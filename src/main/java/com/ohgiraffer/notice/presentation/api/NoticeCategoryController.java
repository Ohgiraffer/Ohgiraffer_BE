package com.ohgiraffer.notice.presentation.api;

import com.ohgiraffer.global.exception.ErrorResponse;
import com.ohgiraffer.notice.application.command.CreateNoticeCategoryCommand;
import com.ohgiraffer.notice.application.usecase.NoticeCategoryCommandUseCase;
import com.ohgiraffer.notice.application.usecase.NoticeCategoryQueryUseCase;
import com.ohgiraffer.notice.domain.model.NoticeCategory;
import com.ohgiraffer.notice.presentation.api.request.CreateNoticeCategoryRequest;
import com.ohgiraffer.notice.presentation.api.response.NoticeCategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Tag(name = "공지 카테고리", description = "공지 작성 화면 드롭다운과 목록 탭에서 사용")
@RestController
@RequestMapping("/notice-categories")
public class NoticeCategoryController {

    private final NoticeCategoryQueryUseCase noticeCategoryQueryUseCase;
    private final NoticeCategoryCommandUseCase noticeCategoryCommandUseCase;

    public NoticeCategoryController(
            NoticeCategoryQueryUseCase noticeCategoryQueryUseCase,
            NoticeCategoryCommandUseCase noticeCategoryCommandUseCase
    ) {
        this.noticeCategoryQueryUseCase = noticeCategoryQueryUseCase;
        this.noticeCategoryCommandUseCase = noticeCategoryCommandUseCase;
    }

    /**
     * 공지 작성 화면의 카테고리 드롭다운과 목록 화면의 탭에서 사용한다.
     */
    @Operation(
            summary = "공지 카테고리 목록 조회",
            description = """
                    등록 순서대로 반환한다. 작성 화면 드롭다운은 미리 선택된 항목 없이 시작한다.
                    화면의 '전체' 탭은 여기에 포함되지 않는다. 필터를 걸지 않는다는 뜻의 화면 라벨이라
                    공지 목록을 categoryId 없이 조회하면 된다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "조회 성공. 비어 있을 수 있다")
    @GetMapping
    public ResponseEntity<List<NoticeCategoryResponse>> findAll() {
        List<NoticeCategoryResponse> categories =
                noticeCategoryQueryUseCase.findAll()
                        .stream()
                        .map(NoticeCategoryResponse::from)
                        .toList();

        return ResponseEntity.ok(categories);
    }

    @Operation(
            summary = "공지 카테고리 등록",
            description = """
                    이름은 중복될 수 없다. 앞뒤 공백은 떼고 저장한다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "등록 성공",
                    content = @Content(schema = @Schema(implementation = NoticeCategoryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이름 누락 또는 50자 초과 (COMMON_001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이름 중복 (NOTICE_005)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'INSTRUCTOR')")
    public ResponseEntity<NoticeCategoryResponse> create(
            @Valid @RequestBody CreateNoticeCategoryRequest request
    ) {
        NoticeCategory created = noticeCategoryCommandUseCase.create(
                new CreateNoticeCategoryCommand(request.name())
        );

        return ResponseEntity
                .created(UriComponentsBuilder
                        .fromPath("/notice-categories/{categoryId}")
                        .buildAndExpand(created.getId())
                        .toUri())
                .body(NoticeCategoryResponse.from(created));
    }

    @Operation(
            summary = "공지 카테고리 삭제",
            description = """
                    공지가 한 건이라도 사용 중이면 삭제할 수 없다.
                    응답 메시지에 몇 건이 막고 있는지 담기므로 화면에 그대로 보여주면 된다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 카테고리 (NOTICE_002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "공지가 사용 중 (NOTICE_006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'INSTRUCTOR')")
    public ResponseEntity<Void> delete(
            @Parameter(description = "카테고리 식별자", example = "2")
            @PathVariable Long categoryId
    ) {
        noticeCategoryCommandUseCase.delete(categoryId);

        return ResponseEntity.noContent().build();
    }
}

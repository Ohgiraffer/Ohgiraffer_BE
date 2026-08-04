package com.ohgiraffer.notice.presentation.api;

import com.ohgiraffer.notice.application.usecase.NoticeCommandUseCase;
import com.ohgiraffer.notice.application.usecase.NoticeQueryUseCase;
import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.presentation.api.request.CreateNoticeRequest;
import com.ohgiraffer.notice.presentation.api.response.CreateNoticeResponse;
import com.ohgiraffer.notice.presentation.api.response.NoticeDetailResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/notices")
public class NoticeController {

    /*
     * 인증 도메인이 아직 없어 작성자를 시드 계정(매니저)으로 고정한다.
     * 인증이 들어오면 currentAuthorId() 한 곳만 교체하면 된다.
     */
    private static final Long TEMPORARY_AUTHOR_ID = 1L;

    private final NoticeCommandUseCase noticeCommandUseCase;
    private final NoticeQueryUseCase noticeQueryUseCase;

    public NoticeController(
            NoticeCommandUseCase noticeCommandUseCase,
            NoticeQueryUseCase noticeQueryUseCase
    ) {
        this.noticeCommandUseCase = noticeCommandUseCase;
        this.noticeQueryUseCase = noticeQueryUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateNoticeResponse> create(
            @Valid @RequestBody CreateNoticeRequest request
    ) {
        Notice notice = noticeCommandUseCase.create(
                request.toCommand(currentAuthorId())
        );

        return ResponseEntity
                .created(URI.create("/notices/" + notice.getId()))
                .body(CreateNoticeResponse.from(notice));
    }

    /**
     * 공지 상세 조회.
     *
     * <p>요구사항상 훈련생에게 비공개인 공지는 노출하지 않아야 하지만,
     * 인증이 없어 요청자의 역할을 알 수 없으므로 아직 필터링하지 않는다.
     * 인증 도입 시 조회 결과의 visibleToTrainee 를 기준으로 차단한다.
     */
    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeDetailResponse> findDetail(
            @PathVariable Long noticeId
    ) {
        return ResponseEntity.ok(
                NoticeDetailResponse.from(
                        noticeQueryUseCase.findDetail(noticeId)
                )
        );
    }

    private Long currentAuthorId() {
        return TEMPORARY_AUTHOR_ID;
    }
}


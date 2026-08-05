package com.ohgiraffer.notice.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notice.application.port.AuthorNameQueryPort;
import com.ohgiraffer.notice.application.query.NoticeDetailView;
import com.ohgiraffer.notice.application.query.NoticeSummaryView;
import com.ohgiraffer.notice.application.usecase.NoticeQueryUseCase;
import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.model.NoticeCategory;
import com.ohgiraffer.notice.domain.model.ViewerRole;
import com.ohgiraffer.notice.domain.repository.NoticeCategoryRepository;
import com.ohgiraffer.notice.domain.repository.NoticeConfirmationRepository;
import com.ohgiraffer.notice.domain.repository.NoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class NoticeQueryService implements NoticeQueryUseCase {

    private final NoticeRepository noticeRepository;
    private final NoticeCategoryRepository noticeCategoryRepository;
    private final NoticeConfirmationRepository noticeConfirmationRepository;
    private final AuthorNameQueryPort authorNameQueryPort;

    public NoticeQueryService(
            NoticeRepository noticeRepository,
            NoticeCategoryRepository noticeCategoryRepository,
            NoticeConfirmationRepository noticeConfirmationRepository,
            AuthorNameQueryPort authorNameQueryPort
    ) {
        this.noticeRepository = noticeRepository;
        this.noticeCategoryRepository = noticeCategoryRepository;
        this.noticeConfirmationRepository = noticeConfirmationRepository;
        this.authorNameQueryPort = authorNameQueryPort;
    }

    @Override
    public NoticeDetailView findDetail(
            Long noticeId,
            ViewerRole viewer,
            Long userId
    ) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(NoticeQueryService::noticeNotFound);

        /*
         * 훈련생 비공개 공지는 403이 아니라 404로 응답한다.
         * 403을 주면 "그 번호의 공지가 존재한다"는 사실이 드러나므로,
         * 요구사항의 "노출하지 않는다"를 만족시키려면 없는 것처럼 보여야 한다.
         */
        if (!notice.isVisibleTo(viewer)) {
            throw noticeNotFound();
        }

        /*
         * 카테고리는 NOT NULL 외래키라 정상 데이터라면 반드시 존재한다.
         * 조회에 실패해도 상세 조회 자체를 막지는 않고 이름만 비워 둔다.
         */
        NoticeCategory category = noticeCategoryRepository
                .findById(notice.getCategoryId())
                .orElse(null);

        /*
         * 확인 정보는 필수 공지에만 의미가 있다. 일반 공지에는 화면에 체크박스가 없다.
         */
        long confirmationCount = 0L;
        boolean confirmedByMe = false;

        if (notice.requiresConfirmation()) {
            confirmationCount =
                    noticeConfirmationRepository.countBy(noticeId);
            confirmedByMe =
                    noticeConfirmationRepository.existsBy(noticeId, userId);
        }

        /*
         * 작성자를 찾지 못해도 상세 조회는 막지 않는다.
         * 탈퇴한 사용자가 쓴 공지도 내용은 그대로 보여야 한다.
         */
        String authorName = authorNameQueryPort
                .findName(notice.getAuthorId())
                .orElse(null);

        return NoticeDetailView.of(
                notice,
                category,
                authorName,
                confirmationCount,
                confirmedByMe
        );
    }

    @Override
    public List<NoticeSummaryView> findAll(
            ViewerRole viewer,
            Long categoryId,
            Long userId
    ) {
        List<Notice> notices =
                noticeRepository.findAllVisible(viewer, categoryId);

        if (notices.isEmpty()) {
            return List.of();
        }

        /*
         * 카테고리는 몇 건 되지 않으므로 한 번에 읽어 이름을 채운다.
         * 공지마다 조회하면 목록 길이만큼 질의가 늘어난다.
         */
        Map<Long, String> categoryNames = noticeCategoryRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        NoticeCategory::getId,
                        NoticeCategory::getName,
                        (first, second) -> first
                ));

        /*
         * 확인 여부도 마찬가지로 한 번에 가져온다.
         * 확인 대상은 필수 공지뿐이라 그 목록만 조회한다.
         */
        List<Long> mandatoryNoticeIds = notices.stream()
                .filter(Notice::requiresConfirmation)
                .map(Notice::getId)
                .toList();

        Set<Long> confirmedNoticeIds = mandatoryNoticeIds.isEmpty()
                ? Set.of()
                : noticeConfirmationRepository
                        .findConfirmedNoticeIds(userId, mandatoryNoticeIds);

        /*
         * 작성자 이름도 한 번에 가져온다.
         * 어댑터가 식별자 중복을 걷어내므로 공지 수가 아니라 사람 수만큼만 질의가 나간다.
         */
        Map<Long, String> authorNames = authorNameQueryPort.findNames(
                notices.stream()
                        .map(Notice::getAuthorId)
                        .toList()
        );

        return notices.stream()
                .map(notice -> NoticeSummaryView.of(
                        notice,
                        categoryNames.get(notice.getCategoryId()),
                        authorNames.get(notice.getAuthorId()),
                        confirmedNoticeIds.contains(notice.getId())
                ))
                .toList();
    }

    private static BusinessException noticeNotFound() {
        return new BusinessException(ErrorCode.NOTICE_NOT_FOUND);
    }
}

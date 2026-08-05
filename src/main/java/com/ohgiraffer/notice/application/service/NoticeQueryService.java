package com.ohgiraffer.notice.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notice.application.query.NoticeDetailView;
import com.ohgiraffer.notice.application.query.NoticeSummaryView;
import com.ohgiraffer.notice.application.usecase.NoticeQueryUseCase;
import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.model.NoticeCategory;
import com.ohgiraffer.notice.domain.model.ViewerRole;
import com.ohgiraffer.notice.domain.repository.NoticeCategoryRepository;
import com.ohgiraffer.notice.domain.repository.NoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class NoticeQueryService implements NoticeQueryUseCase {

    private final NoticeRepository noticeRepository;
    private final NoticeCategoryRepository noticeCategoryRepository;

    public NoticeQueryService(
            NoticeRepository noticeRepository,
            NoticeCategoryRepository noticeCategoryRepository
    ) {
        this.noticeRepository = noticeRepository;
        this.noticeCategoryRepository = noticeCategoryRepository;
    }

    @Override
    public NoticeDetailView findDetail(Long noticeId, ViewerRole viewer) {
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

        return NoticeDetailView.of(notice, category);
    }

    @Override
    public List<NoticeSummaryView> findAll(ViewerRole viewer, Long categoryId) {
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

        return notices.stream()
                .map(notice -> NoticeSummaryView.of(
                        notice,
                        categoryNames.get(notice.getCategoryId())
                ))
                .toList();
    }

    private static BusinessException noticeNotFound() {
        return new BusinessException(ErrorCode.NOTICE_NOT_FOUND);
    }
}

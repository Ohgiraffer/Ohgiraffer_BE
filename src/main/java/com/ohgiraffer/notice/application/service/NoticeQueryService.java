package com.ohgiraffer.notice.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notice.application.query.NoticeDetailView;
import com.ohgiraffer.notice.application.usecase.NoticeQueryUseCase;
import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.model.NoticeCategory;
import com.ohgiraffer.notice.domain.repository.NoticeCategoryRepository;
import com.ohgiraffer.notice.domain.repository.NoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public NoticeDetailView findDetail(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "존재하지 않는 공지입니다."
                ));

        /*
         * 카테고리는 NOT NULL 외래키라 정상 데이터라면 반드시 존재한다.
         * 조회에 실패해도 상세 조회 자체를 막지는 않고 이름만 비워 둔다.
         */
        NoticeCategory category = noticeCategoryRepository
                .findById(notice.getCategoryId())
                .orElse(null);

        return NoticeDetailView.of(notice, category);
    }
}

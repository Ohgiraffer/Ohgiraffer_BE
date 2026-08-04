package com.ohgiraffer.notice.application.usecase;

import com.ohgiraffer.notice.application.query.NoticeDetailView;

/**
 * 공지 조회 유스케이스.
 */
public interface NoticeQueryUseCase {

    NoticeDetailView findDetail(Long noticeId);
}

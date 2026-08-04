package com.ohgiraffer.notice.application.usecase;

import com.ohgiraffer.notice.domain.model.NoticeCategory;

import java.util.List;

/**
 * 공지 카테고리 조회 유스케이스.
 */
public interface NoticeCategoryQueryUseCase {

    List<NoticeCategory> findAll();
}

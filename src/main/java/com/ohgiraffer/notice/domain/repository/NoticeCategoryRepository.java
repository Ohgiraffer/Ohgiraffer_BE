package com.ohgiraffer.notice.domain.repository;

import com.ohgiraffer.notice.domain.model.NoticeCategory;

import java.util.List;
import java.util.Optional;

/**
 * 공지 카테고리 영속성 포트.
 *
 * <p>notice.notice_category_id 가 NOT NULL 외래키라, 공지를 저장하기 전에
 * 카테고리 존재 여부를 확인해 제약 위반 대신 명확한 오류를 돌려주기 위해 사용한다.
 */
public interface NoticeCategoryRepository {

    boolean existsById(Long categoryId);

    Optional<NoticeCategory> findById(Long categoryId);

    /**
     * 공지 작성 화면의 카테고리 드롭다운에 쓰인다. 등록 순서대로 반환한다.
     */
    List<NoticeCategory> findAll();
}

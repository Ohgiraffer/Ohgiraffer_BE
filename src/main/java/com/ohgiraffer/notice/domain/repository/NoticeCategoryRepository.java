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

    NoticeCategory save(NoticeCategory category);

    void deleteById(Long categoryId);

    /**
     * 이름 중복 검사. name 에 UNIQUE 제약이 있어 DB도 막지만,
     * 제약 위반 예외는 500 으로 새기 때문에 저장 전에 걸러 409 로 알린다.
     */
    boolean existsByName(String name);
}

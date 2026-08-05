package com.ohgiraffer.notice.application.usecase;

import com.ohgiraffer.notice.application.command.CreateNoticeCategoryCommand;
import com.ohgiraffer.notice.domain.model.NoticeCategory;

/**
 * 공지 카테고리 관리 유스케이스.
 *
 * <p>관리 화면이 등록과 삭제만 제공하므로 수정은 없다.
 * 이름을 바꾸려면 새로 만들고 쓰지 않는 것을 지우는 방식이 된다.
 */
public interface NoticeCategoryCommandUseCase {

    NoticeCategory create(CreateNoticeCategoryCommand command);

    /**
     * 카테고리를 삭제한다. 공지가 한 건이라도 사용 중이면 삭제하지 않는다.
     */
    void delete(Long categoryId);
}

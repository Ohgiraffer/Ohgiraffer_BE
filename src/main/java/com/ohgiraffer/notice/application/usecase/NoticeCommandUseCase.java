package com.ohgiraffer.notice.application.usecase;

import com.ohgiraffer.notice.application.command.CreateNoticeCommand;
import com.ohgiraffer.notice.application.command.UpdateNoticeCommand;
import com.ohgiraffer.notice.application.query.NoticeConfirmationView;
import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.model.ViewerRole;

/**
 * 공지 쓰기 유스케이스. 다른 도메인이 공지를 다뤄야 할 때도 이 인터페이스만 호출한다.
 */
public interface NoticeCommandUseCase {

    Notice create(CreateNoticeCommand command);

    Notice update(UpdateNoticeCommand command);

    /**
     * 공지를 완전히 삭제한다. 요구사항상 하드 딜리트이며 첨부파일과 AI 일정 후보도 함께 지워진다.
     *
     * @param requesterId 삭제를 요청한 사용자. 작성자 본인이어야 한다
     */
    void delete(Long noticeId, Long requesterId);

    /**
     * 고정 공지를 확인 처리하고 갱신된 확인 현황을 돌려준다.
     *
     * <p>이미 확인했다면 새로 기록하지 않고 현재 현황만 돌려준다.
     *
     * @param viewer 조회자 구분. 훈련생에게 비공개인 공지는 확인할 수 없다
     */
    NoticeConfirmationView confirm(
            Long noticeId,
            ViewerRole viewer,
            Long userId
    );
}

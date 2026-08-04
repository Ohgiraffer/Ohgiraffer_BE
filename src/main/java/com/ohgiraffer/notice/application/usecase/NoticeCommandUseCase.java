package com.ohgiraffer.notice.application.usecase;

import com.ohgiraffer.notice.application.command.CreateNoticeCommand;
import com.ohgiraffer.notice.domain.model.Notice;

/**
 * 공지 쓰기 유스케이스. 다른 도메인이 공지를 다뤄야 할 때도 이 인터페이스만 호출한다.
 */
public interface NoticeCommandUseCase {

    Notice create(CreateNoticeCommand command);
}

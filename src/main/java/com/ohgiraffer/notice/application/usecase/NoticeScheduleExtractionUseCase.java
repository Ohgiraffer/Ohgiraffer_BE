package com.ohgiraffer.notice.application.usecase;

import com.ohgiraffer.notice.domain.model.ExtractedSchedule;

import java.util.List;

public interface NoticeScheduleExtractionUseCase {

    /**
     * 공지 본문에서 캘린더에 넣을 만한 일정 후보를 찾는다.
     *
     * <p>찾기만 하고 저장하지 않는다. 사람이 모달에서 고치거나 뺄 수 있어야 해서,
     * 여기서 저장해 두면 버려질 값을 남기게 된다.
     *
     * @return 후보 목록. 찾지 못하면 빈 목록
     */
    List<ExtractedSchedule> extract(Long noticeId);
}

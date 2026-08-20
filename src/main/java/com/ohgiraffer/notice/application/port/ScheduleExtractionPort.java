package com.ohgiraffer.notice.application.port;

import com.ohgiraffer.notice.domain.model.ExtractedSchedule;

import java.time.LocalDate;
import java.util.List;

/**
 * 공지 본문에서 일정 후보를 찾아낸다.
 *
 * <p>어떤 모델을 쓰는지는 어댑터가 정한다. 응용 계층은 "본문을 넘기면 후보가 나온다" 까지만 안다.
 */
public interface ScheduleExtractionPort {

    /**
     * @param baseDate 연도가 적히지 않은 날짜를 해석할 기준일. 보통 공지 작성일이다.
     *                 "8월 5일" 처럼 적힌 일정은 이 값이 없으면 어느 해인지 정할 수 없다
     * @return 찾아낸 후보. 없으면 빈 목록
     */
    List<ExtractedSchedule> extract(String title, String content, LocalDate baseDate);
}

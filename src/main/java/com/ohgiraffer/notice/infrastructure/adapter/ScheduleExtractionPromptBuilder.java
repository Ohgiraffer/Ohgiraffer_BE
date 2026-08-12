package com.ohgiraffer.notice.infrastructure.adapter;

import java.time.LocalDate;

/**
 * 공지 본문을 제미나이에게 보낼 글로 바꾼다.
 *
 * <p>답을 JSON 으로 받는다. 문장으로 받으면 날짜와 시각을 다시 뽑아내야 하는데,
 * 그 파싱이 AI 응답만큼이나 틀리기 쉽다.
 *
 * <p>작성일을 함께 넘기는 이유는 연도 때문이다. 공지에는 "8월 5일" 처럼 해를 빼고 적는 일이
 * 흔한데, 기준이 없으면 모델이 아무 해나 고른다.
 */
final class ScheduleExtractionPromptBuilder {

    private ScheduleExtractionPromptBuilder() {
    }

    static String build(String title, String content, LocalDate baseDate) {
        return """
                너는 부트캠프 운영진을 돕는 조수다.
                아래 공지사항에서 캘린더에 등록할 만한 일정을 찾아라.

                이 공지는 %s 에 작성되었다. 연도가 적혀 있지 않은 날짜는 이 날을 기준으로 해석해라.

                JSON 배열만 출력해라. 설명, 인사말, 코드 블록 표시를 붙이지 마라.
                일정이 없으면 빈 배열 [] 만 출력해라.

                배열의 각 원소는 이런 모양이다.
                {
                  "title": "일정명",
                  "eventType": "CLASS | PRESENTATION | ASSIGNMENT | EVENT | null",
                  "startDate": "2026-08-05",
                  "startTime": "10:00" 또는 null,
                  "endDate": "2026-08-05",
                  "endTime": "12:00" 또는 null,
                  "location": "장소" 또는 null
                }

                지켜야 할 것.
                - 본문에 없는 일정을 지어내지 마라. 찾지 못하면 빈 배열이 맞다.
                - startDate 와 endDate 는 반드시 채워라. 하루짜리면 두 값이 같다.
                - 시각이 적혀 있지 않으면 startTime 과 endTime 을 null 로 둬라.
                  "오전 중" 처럼 범위가 흐릿한 표현도 null 이다. 임의로 09:00 같은 값을 넣지 마라.
                - eventType 은 확신이 설 때만 채워라. 애매하면 null 로 둬라.
                  사람이 고르면 되는 값이라, 틀린 값을 채우는 것보다 비우는 편이 낫다.
                  수업/강의는 CLASS, 발표는 PRESENTATION, 과제 제출 마감은 ASSIGNMENT,
                  그 밖의 행사는 EVENT 다.
                - 장소가 적혀 있지 않으면 location 을 null 로 둬라.
                - 접수 기간, 신청 마감처럼 훈련생이 날짜를 챙겨야 하는 것도 일정으로 본다.

                제목:
                %s

                본문:
                %s
                """.formatted(baseDate, title, content);
    }
}

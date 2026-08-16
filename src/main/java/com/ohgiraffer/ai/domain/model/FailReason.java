package com.ohgiraffer.ai.domain.model;

public enum FailReason {
    RATE_LIMIT, // 요청이 너무 많거나 결제 크레딧이 소진됐을 때
    AUTH_INVALID, // API 키 자체가 잘못됐거나, 만료됐거나, 권한이 없는 경우
    BAD_REQUEST, // 구글에 보낸 요청 본문의 형식이 잘못됐을 때
    EMPTY_RESPONSE, // 구글 서버가 응답은 줬는데 내용이 비어있는 이상 케이스
    SERVER_ERROR // 구글 Gemini 서버 자체의 일시적 장애
}
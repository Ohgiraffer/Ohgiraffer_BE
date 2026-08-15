package com.ohgiraffer.notice.application.port;

/**
 * 화면이 제미나이를 직접 부를 때 쓸 접속 정보를 가져온다.
 *
 * <p>포트로 나눈 이유는 공지 도메인이 AI 설정을 직접 읽지 않게 하기 위해서다. 설정 이름이
 * 바뀌거나 키를 발급하는 방식이 달라져도 어댑터만 고치면 된다.
 */
public interface AiCredentialPort {

    /**
     * @param apiKey 제미나이 API 키
     * @param model  호출할 모델 이름
     */
    record AiCredential(String apiKey, String model) {
    }

    AiCredential get();
}

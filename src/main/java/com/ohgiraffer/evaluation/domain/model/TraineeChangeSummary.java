package com.ohgiraffer.evaluation.domain.model;

/**
 * 훈련생 한 명이 이번 동기화에서 어떻게 달라졌는지.
 *
 * <p>화면이 훈련생별 카드로 그리므로 값으로 들고 있는다. 문장 하나로 내려주면 화면이
 * 그것을 다시 잘라 카드를 만들 수 없다.
 *
 * <p>한 사람이 여러 항목에서 바뀌었어도 카드는 하나다. 운영진이 알고 싶은 것은
 * "이 훈련생에게 무슨 일이 있었나" 이지 항목별 나열이 아니다. 대신 어느 항목이
 * 바뀌었는지는 {@code item} 에 모아 적는다.
 *
 * <p>{@code score} 와 {@code comment} 는 바뀐 값을 그대로 적은 글이다. AI 가 만들지 않아
 * 외부 호출이 실패해도 카드는 나온다. AI 는 {@code needsCheck} 한 줄만 채운다.
 *
 * @param needsCheck 운영진이 확인해야 할 점. AI 가 짚어낸 것이라 없을 수 있다
 */
public record TraineeChangeSummary(
        String traineeName,
        String evaluationType,
        String item,
        String score,
        String comment,
        String needsCheck
) {

    /**
     * 확인 필요만 갈아끼운 카드를 돌려준다. 나머지 값은 AI 와 무관하게 이미 정해져 있다.
     */
    public TraineeChangeSummary withNeedsCheck(String needsCheck) {
        return new TraineeChangeSummary(
                traineeName, evaluationType, item, score, comment, needsCheck
        );
    }
}

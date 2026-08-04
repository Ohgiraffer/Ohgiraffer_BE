package com.ohgiraffer.survey.domain.model;

public enum SurveyFormStatus {

    /*
     * Google Form은 생성되었지만
     * 아직 훈련생에게 공개되지 않은 상태
     */
    DRAFT,

    /*
     * 훈련생에게 공개되어
     * 응답을 받을 수 있는 상태
     */
    PUBLISHED,

    /*
     * 응답 마감일이 지났거나
     * 운영진이 응답 접수를 종료한 상태
     */
    CLOSED
}
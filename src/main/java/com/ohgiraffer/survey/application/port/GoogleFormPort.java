package com.ohgiraffer.survey.application.port;

public interface GoogleFormPort {

    /*
     * 제목을 이용하여 아직 게시되지 않은
     * 빈 Google Form 초안을 생성합니다.
     */
    CreatedGoogleForm createDraft(
            String title
    );

    void updateTitle(
            String googleFormId,
            String title
    );

    void updatePublishState(
            String googleFormId,
            boolean published,
            boolean acceptingResponses
    );

    /*
     * Google Form을 삭제합니다.
     *
     * 설문 생성 과정에서 DB 저장이 실패했을 경우
     * 이미 생성된 Google Form이 고아 파일로 남지 않도록
     * 보상 처리에도 사용합니다.
     */
    void delete(
            String googleFormId
    );
}
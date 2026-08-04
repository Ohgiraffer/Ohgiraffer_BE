package com.ohgiraffer.survey.domain.repository;

import com.ohgiraffer.survey.domain.model.SurveyForm;

import java.util.List;
import java.util.Optional;

public interface SurveyFormRepository {

    /*
     * 설문 폼을 저장하고,
     * DB에서 생성된 ID와 시간을 포함한 도메인 객체를 반환합니다.
     */
    SurveyForm save(SurveyForm surveyForm);

    List<SurveyForm> findAll();

    /*
     * 설문 폼 단건 조회 API에서 사용합니다.
     */
    Optional<SurveyForm> findById(Long surveyFormId);


    /*
     * 동일한 Google Form ID가 이미 저장되어 있는지 확인합니다.
     */
    boolean existsByGoogleFormId(String googleFormId);

    /*
     * 설문 삭제 API에서 사용합니다.
     */
    void delete(SurveyForm surveyForm);
}
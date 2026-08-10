package com.ohgiraffer.evaluation.application.port;

import java.util.List;

/**
 * 시트에서 평가 행을 읽어온다.
 *
 * <p>공용 클라이언트는 시트의 의미를 모르므로 문자열 표만 돌려준다. 그것을 평가로
 * 해석하는 일은 이 도메인이 한다.
 */
public interface EvaluationSheetReaderPort {

    /**
     * 탭 전체를 표 형태로 읽는다. 첫 줄은 헤더다.
     *
     * <p>셀마다 부르지 않고 범위 하나로 묶어 부른다. 구글 시트 호출 한도가 서비스 계정
     * 전체로 분당 60회라, 행 수와 무관하게 한 번에 끝나야 한다.
     */
    List<List<String>> readRows(String spreadsheetUrl, String tabName);
}

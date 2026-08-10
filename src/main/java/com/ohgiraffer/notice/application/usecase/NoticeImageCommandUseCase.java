package com.ohgiraffer.notice.application.usecase;

import org.springframework.web.multipart.MultipartFile;

public interface NoticeImageCommandUseCase {

    /**
     * 공지 본문에 넣을 이미지를 올리고, 본문에 그대로 박아 쓸 주소를 돌려준다.
     *
     * <p>글을 쓰는 중에 호출되므로 공지 번호를 받지 않는다.
     *
     * @return 만료되지 않는 이미지 주소
     */
    String upload(MultipartFile image);

    /**
     * 본문 이미지 경로의 파일명을 저장소의 실제 주소로 바꾼다.
     *
     * @param fileName 업로드 때 돌려준 경로의 마지막 조각
     * @return 잠시 유효한 저장소 주소
     */
    String resolveUrl(String fileName);
}

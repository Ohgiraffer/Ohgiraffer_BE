package com.ohgiraffer.notice.domain.repository;

import com.ohgiraffer.notice.domain.model.Notice;

import java.util.Optional;

/**
 * 공지 영속성 포트. 구현은 infrastructure 계층의 어댑터가 담당한다.
 */
public interface NoticeRepository {

    Notice save(Notice notice);

    Optional<Notice> findById(Long noticeId);
}

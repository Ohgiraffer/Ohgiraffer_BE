package com.ohgiraffer.notice.application.command;

/**
 * 공지 카테고리 등록 명령.
 *
 * <p>관리 화면에 이름 입력칸 하나뿐이라 이름만 받는다.
 */
public record CreateNoticeCategoryCommand(String name) {
}

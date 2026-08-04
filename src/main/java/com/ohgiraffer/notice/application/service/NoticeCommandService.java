package com.ohgiraffer.notice.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notice.application.command.CreateNoticeCommand;
import com.ohgiraffer.notice.application.usecase.NoticeCommandUseCase;
import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.repository.NoticeCategoryRepository;
import com.ohgiraffer.notice.domain.repository.NoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NoticeCommandService implements NoticeCommandUseCase {

    private final NoticeRepository noticeRepository;
    private final NoticeCategoryRepository noticeCategoryRepository;

    public NoticeCommandService(
            NoticeRepository noticeRepository,
            NoticeCategoryRepository noticeCategoryRepository
    ) {
        this.noticeRepository = noticeRepository;
        this.noticeCategoryRepository = noticeCategoryRepository;
    }

    @Override
    public Notice create(CreateNoticeCommand command) {
        validateCategoryExists(command.categoryId());

        Notice notice = Notice.create(
                command.authorId(),
                command.categoryId(),
                command.title(),
                command.content(),
                command.mandatory(),
                command.visibleToTrainee()
        );

        return noticeRepository.save(notice);
    }

    private void validateCategoryExists(Long categoryId) {
        if (categoryId == null) {
            return;
        }

        if (!noticeCategoryRepository.existsById(categoryId)) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "존재하지 않는 공지 카테고리입니다."
            );
        }
    }
}

package com.ohgiraffer.notice.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notice.application.command.CreateNoticeCategoryCommand;
import com.ohgiraffer.notice.application.usecase.NoticeCategoryCommandUseCase;
import com.ohgiraffer.notice.domain.model.NoticeCategory;
import com.ohgiraffer.notice.domain.repository.NoticeCategoryRepository;
import com.ohgiraffer.notice.domain.repository.NoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NoticeCategoryCommandService implements NoticeCategoryCommandUseCase {

    private final NoticeCategoryRepository noticeCategoryRepository;
    private final NoticeRepository noticeRepository;

    public NoticeCategoryCommandService(
            NoticeCategoryRepository noticeCategoryRepository,
            NoticeRepository noticeRepository
    ) {
        this.noticeCategoryRepository = noticeCategoryRepository;
        this.noticeRepository = noticeRepository;
    }

    @Override
    public NoticeCategory create(CreateNoticeCategoryCommand command) {
        NoticeCategory category = NoticeCategory.create(command.name());

        /*
         * 이름 검증을 통과한 뒤에 중복을 본다.
         * 도메인이 앞뒤 공백을 떼므로, 다듬어진 이름으로 검사해야 " 수업 " 이 빠져나가지 않는다.
         */
        if (noticeCategoryRepository.existsByName(category.getName())) {
            throw new BusinessException(
                    ErrorCode.NOTICE_CATEGORY_DUPLICATE_NAME
            );
        }

        return noticeCategoryRepository.save(category);
    }

    @Override
    public void delete(Long categoryId) {
        requireCategoryExists(categoryId);

        /*
         * 외래키에 ON DELETE 절이 없어 사용 중이면 DB가 제약 위반을 던진다.
         * 그대로 두면 500 이 되므로, 몇 건이 막고 있는지 세어 409 로 알려준다.
         * 화면은 이 규칙으로 휴지통 버튼을 잠그지만, 목록을 띄운 뒤 다른 사람이 공지를 쓸 수 있어
         * 서버에서도 같은 조건을 다시 본다.
         */
        long usedBy = noticeRepository.countByCategoryId(categoryId);

        if (usedBy > 0) {
            throw new BusinessException(
                    ErrorCode.NOTICE_CATEGORY_IN_USE,
                    "공지 " + usedBy + "건이 사용 중이라 삭제할 수 없습니다."
                            + " 해당 공지의 카테고리를 먼저 옮겨주세요."
            );
        }

        noticeCategoryRepository.deleteById(categoryId);
    }

    private void requireCategoryExists(Long categoryId) {
        if (!noticeCategoryRepository.existsById(categoryId)) {
            throw new BusinessException(
                    ErrorCode.NOTICE_CATEGORY_NOT_FOUND
            );
        }
    }
}

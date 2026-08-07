package com.ohgiraffer.notice.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.s3.S3FileHandler;
import com.ohgiraffer.notice.application.command.CreateNoticeCommand;
import com.ohgiraffer.notice.application.command.UpdateNoticeCommand;
import com.ohgiraffer.notice.application.query.NoticeConfirmationView;
import com.ohgiraffer.notice.application.usecase.NoticeCommandUseCase;
import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.model.NoticeAttachment;
import com.ohgiraffer.notice.domain.model.ViewerRole;
import com.ohgiraffer.notice.domain.repository.NoticeAttachmentRepository;
import com.ohgiraffer.notice.domain.repository.NoticeCategoryRepository;
import com.ohgiraffer.notice.domain.repository.NoticeConfirmationRepository;
import com.ohgiraffer.notice.domain.repository.NoticeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NoticeCommandService implements NoticeCommandUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(NoticeCommandService.class);

    private final NoticeRepository noticeRepository;
    private final NoticeCategoryRepository noticeCategoryRepository;
    private final NoticeConfirmationRepository noticeConfirmationRepository;
    private final NoticeAttachmentRepository noticeAttachmentRepository;
    private final S3FileHandler s3FileHandler;

    public NoticeCommandService(
            NoticeRepository noticeRepository,
            NoticeCategoryRepository noticeCategoryRepository,
            NoticeConfirmationRepository noticeConfirmationRepository,
            NoticeAttachmentRepository noticeAttachmentRepository,
            S3FileHandler s3FileHandler
    ) {
        this.noticeRepository = noticeRepository;
        this.noticeCategoryRepository = noticeCategoryRepository;
        this.noticeConfirmationRepository = noticeConfirmationRepository;
        this.noticeAttachmentRepository = noticeAttachmentRepository;
        this.s3FileHandler = s3FileHandler;
    }

    @Override
    public Notice create(CreateNoticeCommand command) {
        validateCategoryExists(command.categoryId());

        Notice notice = Notice.create(
                command.authorId(),
                command.categoryId(),
                command.title(),
                command.content(),
                command.pinned(),
                command.visibleToTrainee()
        );

        return noticeRepository.save(notice);
    }

    @Override
    public Notice update(UpdateNoticeCommand command) {
        Notice notice = findNotice(command.noticeId());
        requireAuthor(notice, command.editorId());
        validateCategoryExists(command.categoryId());

        Notice updated = notice.update(
                command.categoryId(),
                command.title(),
                command.content(),
                command.pinned(),
                command.visibleToTrainee()
        );

        return noticeRepository.update(updated);
    }

    @Override
    public void delete(Long noticeId, Long requesterId) {
        Notice notice = findNotice(noticeId);
        requireAuthor(notice, requesterId);

        /*
         * 첨부 행은 외래키의 ON DELETE CASCADE 로 함께 지워지지만 S3 객체는 남는다.
         * DB 가 정리해 주지 않는 쪽이라 지우기 전에 키를 읽어 두고 직접 지운다.
         */
        List<NoticeAttachment> attachments =
                noticeAttachmentRepository.findAllByNoticeId(noticeId);

        noticeRepository.deleteById(noticeId);

        for (NoticeAttachment attachment : attachments) {
            deleteQuietly(attachment.getFileKey());
        }
    }

    /**
     * 저장소 정리는 실패해도 공지 삭제를 되돌리지 않는다.
     *
     * <p>사용자가 요청한 것은 공지 삭제이고 그것은 이미 끝났다. 여기서 예외를 던지면
     * 지워진 공지에 대해 실패를 알리게 된다. 남은 객체는 로그로 남겨 나중에 정리한다.
     */
    private void deleteQuietly(String fileKey) {
        try {
            s3FileHandler.delete(fileKey);
        } catch (RuntimeException exception) {
            log.warn(
                    "공지 삭제 후 첨부파일 S3 객체를 정리하지 못했습니다. key={}",
                    fileKey,
                    exception
            );
        }
    }

    @Override
    public NoticeConfirmationView confirm(
            Long noticeId,
            ViewerRole viewer,
            Long userId
    ) {
        Notice notice = findNotice(noticeId);

        /*
         * 공개 대상 확인이 가장 먼저다. 상세 조회와 같은 이유로 403 이 아니라 404 로 답한다.
         *
         * 고정 여부보다 먼저 보는 것도 일부러다. 순서를 바꾸면 훈련생이 못 보는 공지에
         * 필수는 404, 일반은 400 이 돌아가 응답만으로 그 공지의 성격을 알아낼 수 있다.
         */
        if (!notice.isVisibleTo(viewer)) {
            throw new BusinessException(ErrorCode.NOTICE_NOT_FOUND);
        }

        noticeConfirmationRepository.confirm(noticeId, userId);

        /*
         * 방금 저장한 건까지 세야 하므로 확인 처리 뒤에 읽는다.
         * 확인은 취소할 수 없으니 이 시점의 confirmedByMe 는 언제나 true 다.
         */
        return new NoticeConfirmationView(
                noticeId,
                noticeConfirmationRepository.countBy(noticeId),
                true
        );
    }

    private Notice findNotice(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
    }

    /**
     * 조회와 달리 여기서는 403으로 응답한다. 운영진은 어차피 그 공지를 볼 수 있으므로
     * 존재 사실을 숨길 이유가 없고, "남의 공지라 못 고친다"를 알려주는 편이 낫다.
     */
    private void requireAuthor(Notice notice, Long userId) {
        if (!notice.isAuthoredBy(userId)) {
            throw new BusinessException(ErrorCode.NOTICE_NOT_AUTHOR);
        }
    }

    private void validateCategoryExists(Long categoryId) {
        if (categoryId == null) {
            return;
        }

        if (!noticeCategoryRepository.existsById(categoryId)) {
            throw new BusinessException(ErrorCode.NOTICE_CATEGORY_NOT_FOUND);
        }
    }
}

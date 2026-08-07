package com.ohgiraffer.notice.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.s3.S3FileHandler;
import com.ohgiraffer.notice.application.command.CreateNoticeCommand;
import com.ohgiraffer.notice.application.command.NoticeAttachmentCommand;
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
import java.util.Set;
import java.util.stream.Collectors;

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
        validateAttachments(command.attachments());

        Notice notice = Notice.create(
                command.authorId(),
                command.categoryId(),
                command.title(),
                command.content(),
                command.pinned(),
                command.visibleToTrainee()
        );

        Notice saved = noticeRepository.save(notice);

        /*
         * 파일은 등록 화면에서 고를 때 이미 저장소에 올라가 있고, 여기서는 그것을 공지에 잇는다.
         * 공지 저장과 같은 트랜잭션이라 둘 중 하나만 남는 일이 없다.
         */
        if (!command.attachments().isEmpty()) {
            noticeAttachmentRepository.saveAll(
                    command.attachments().stream()
                            .map(attachment -> NoticeAttachment.create(
                                    saved.getId(),
                                    attachment.fileKey(),
                                    attachment.fileName(),
                                    attachment.fileSizeBytes(),
                                    attachment.fileType()
                            ))
                            .toList()
            );
        }

        return saved;
    }

    private void validateAttachments(List<NoticeAttachmentCommand> attachments) {
        if (attachments.size() > NoticeAttachment.MAX_COUNT_PER_NOTICE) {
            throw new BusinessException(
                    ErrorCode.NOTICE_ATTACHMENT_COUNT_EXCEEDED,
                    "공지 하나에는 첨부파일을 "
                            + NoticeAttachment.MAX_COUNT_PER_NOTICE
                            + "개까지 올릴 수 있습니다."
            );
        }

        /*
         * 저장 키는 클라이언트를 거쳐 들어오므로 같은 키가 두 번 실려 올 수 있다.
         * 그대로 두면 한쪽 공지를 지울 때 저장소 객체가 사라져 다른 공지의 첨부가 깨진다.
         * 화면이 업로드 상태를 비우지 않고 공지를 연달아 등록하면 실제로 일어난다.
         */
        Set<String> fileKeys = attachments.stream()
                .map(NoticeAttachmentCommand::fileKey)
                .collect(Collectors.toSet());

        if (fileKeys.size() != attachments.size()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "같은 파일을 두 번 첨부할 수 없습니다."
            );
        }

        for (String fileKey : fileKeys) {
            if (noticeAttachmentRepository.existsByFileKey(fileKey)) {
                throw new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "이미 다른 공지가 사용 중인 파일입니다."
                );
            }
        }
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
         * DB 가 정리해 주지 않는 쪽이라 지우기 전에 키를 읽어 둔다.
         */
        List<String> fileKeys = noticeAttachmentRepository
                .findAllByNoticeId(noticeId)
                .stream()
                .map(NoticeAttachment::getFileKey)
                .toList();

        noticeRepository.deleteById(noticeId);

        /*
         * 저장소 삭제는 커밋된 뒤에 한다. 커밋 전에 지우면 그 뒤 트랜잭션이 되돌아갔을 때
         * 공지와 첨부 행은 살아나는데 파일만 사라진다.
         */
        AfterCommit.run(() -> fileKeys.forEach(this::deleteQuietly));
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

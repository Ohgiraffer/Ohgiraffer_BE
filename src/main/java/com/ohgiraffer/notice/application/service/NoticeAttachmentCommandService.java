package com.ohgiraffer.notice.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.s3.S3FileHandler;
import com.ohgiraffer.global.s3.S3KeyGenerator;
import com.ohgiraffer.notice.application.usecase.NoticeAttachmentCommandUseCase;
import com.ohgiraffer.notice.domain.model.Notice;
import com.ohgiraffer.notice.domain.model.NoticeAttachment;
import com.ohgiraffer.notice.domain.repository.NoticeAttachmentRepository;
import com.ohgiraffer.notice.domain.repository.NoticeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class NoticeAttachmentCommandService
        implements NoticeAttachmentCommandUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(NoticeAttachmentCommandService.class);

    private final NoticeRepository noticeRepository;
    private final NoticeAttachmentRepository noticeAttachmentRepository;
    private final S3FileHandler s3FileHandler;

    public NoticeAttachmentCommandService(
            NoticeRepository noticeRepository,
            NoticeAttachmentRepository noticeAttachmentRepository,
            S3FileHandler s3FileHandler
    ) {
        this.noticeRepository = noticeRepository;
        this.noticeAttachmentRepository = noticeAttachmentRepository;
        this.s3FileHandler = s3FileHandler;
    }

    @Override
    public List<NoticeAttachment> upload(
            Long noticeId,
            Long requesterId,
            List<MultipartFile> files
    ) {
        Notice notice = findNotice(noticeId);
        requireAuthor(notice, requesterId);

        validateFiles(noticeId, files);

        /*
         * S3 는 트랜잭션에 참여하지 않는다. DB 저장이 실패해도 이미 올라간 객체는 남으므로,
         * 올린 키를 들고 있다가 실패하면 직접 지운다. 순서를 뒤집어 DB 를 먼저 쓰면
         * 업로드 실패 시 실체 없는 행이 남아 더 나쁘다.
         */
        List<String> uploadedKeys = new ArrayList<>();

        try {
            List<NoticeAttachment> attachments = new ArrayList<>();

            for (MultipartFile file : files) {
                String key = S3KeyGenerator.noticeAttachmentKey(
                        noticeId,
                        file.getOriginalFilename()
                );

                s3FileHandler.upload(file, key);
                uploadedKeys.add(key);

                attachments.add(NoticeAttachment.create(
                        noticeId,
                        key,
                        file.getOriginalFilename(),
                        file.getSize(),
                        file.getContentType()
                ));
            }

            return noticeAttachmentRepository.saveAll(attachments);

        } catch (RuntimeException exception) {
            deleteQuietly(uploadedKeys);
            throw exception;
        }
    }

    @Override
    public void delete(
            Long noticeId,
            Long noticeAttachmentId,
            Long requesterId
    ) {
        Notice notice = findNotice(noticeId);
        requireAuthor(notice, requesterId);

        NoticeAttachment attachment =
                noticeAttachmentRepository.findById(noticeAttachmentId)
                        .orElseThrow(() -> new BusinessException(
                                ErrorCode.NOTICE_ATTACHMENT_NOT_FOUND));

        /*
         * 경로의 공지와 첨부가 실제로 맞물리는지 본다. 확인하지 않으면 자기 공지 번호에
         * 남의 첨부 번호를 붙여 지울 수 있다.
         */
        if (!attachment.belongsTo(noticeId)) {
            throw new BusinessException(ErrorCode.NOTICE_ATTACHMENT_NOT_FOUND);
        }

        noticeAttachmentRepository.deleteById(noticeAttachmentId);

        /*
         * S3 삭제는 DB 삭제 뒤에 한다. 반대로 하면 DB 삭제가 실패했을 때
         * 파일 없는 첨부 행이 화면에 남는다.
         */
        deleteQuietly(List.of(attachment.getFileKey()));
    }

    private void validateFiles(Long noticeId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "첨부할 파일이 필요합니다."
            );
        }

        long existingCount =
                noticeAttachmentRepository.countByNoticeId(noticeId);

        if (existingCount + files.size()
                > NoticeAttachment.MAX_COUNT_PER_NOTICE) {
            throw new BusinessException(
                    ErrorCode.NOTICE_ATTACHMENT_COUNT_EXCEEDED,
                    "공지 하나에는 첨부파일을 "
                            + NoticeAttachment.MAX_COUNT_PER_NOTICE
                            + "개까지 올릴 수 있습니다. 현재 "
                            + existingCount + "개가 있습니다."
            );
        }

        /*
         * 크기는 한 건이라도 올리기 전에 전부 확인한다. 올리면서 검사하면
         * 마지막 파일에서 걸렸을 때 앞의 파일들을 되돌려야 한다.
         */
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "빈 파일은 첨부할 수 없습니다."
                );
            }

            NoticeAttachment.validateFileSize(file.getSize());
            NoticeAttachment.validateFileType(file.getOriginalFilename());
        }
    }

    /**
     * S3 정리는 실패해도 흐름을 막지 않는다.
     *
     * <p>여기서 다시 예외를 던지면 원래의 실패 원인이 가려지고, 삭제 흐름에서는
     * 이미 지워진 DB 행이 되살아나지 않아 사용자에게 알릴 것도 없다.
     * 남은 객체는 비용만 차지하므로 로그로 남겨 나중에 정리한다.
     */
    private void deleteQuietly(List<String> keys) {
        for (String key : keys) {
            try {
                s3FileHandler.delete(key);
            } catch (RuntimeException exception) {
                log.warn(
                        "공지 첨부파일 S3 객체를 정리하지 못했습니다. key={}",
                        key,
                        exception
                );
            }
        }
    }

    private Notice findNotice(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
    }

    private void requireAuthor(Notice notice, Long userId) {
        if (!notice.isAuthoredBy(userId)) {
            throw new BusinessException(ErrorCode.NOTICE_NOT_AUTHOR);
        }
    }
}

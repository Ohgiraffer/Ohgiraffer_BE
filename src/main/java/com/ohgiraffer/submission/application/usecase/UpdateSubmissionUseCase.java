package com.ohgiraffer.submission.application.usecase;

import com.ohgiraffer.submission.application.command.UpdateSubmissionCommand;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UpdateSubmissionUseCase {

    UpdateSubmissionResult update(
            UpdateSubmissionCommand command,
            List<MultipartFile> files
    );
}